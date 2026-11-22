package com.lyriclearn.ledger.service;

import com.lyriclearn.common.BizException;
import com.lyriclearn.evidence.EvidenceService;
import com.lyriclearn.ledger.config.PointsProperties;
import com.lyriclearn.ledger.entity.LearningAccount;
import com.lyriclearn.ledger.entity.PointTransaction;
import com.lyriclearn.ledger.repo.LearningAccountRepository;
import com.lyriclearn.ledger.repo.PointTransactionRepository;
import com.lyriclearn.omniedu.OmniEduAdapter;
import com.lyriclearn.omniedu.OmniEduDto.Assertion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 积分账本服务（架构 9）：复式记账、乐观锁并发、幂等、公式结算、单日额度硬截断、兑换事务。
 * 账本写入与存证解耦：本地事务完成后异步提交叶子哈希，存证失败不回滚发放。
 */
@Service
public class PointLedgerService {

    private static final Logger log = LoggerFactory.getLogger(PointLedgerService.class);
    private static final int MAX_RETRY = 3;

    private final LearningAccountRepository accountRepository;
    private final PointTransactionRepository transactionRepository;
    private final EvidenceService evidenceService;
    private final OmniEduAdapter omniEdu;
    private final PointsProperties props;

    public PointLedgerService(LearningAccountRepository accountRepository,
                              PointTransactionRepository transactionRepository,
                              EvidenceService evidenceService,
                              OmniEduAdapter omniEdu,
                              PointsProperties props) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.evidenceService = evidenceService;
        this.omniEdu = omniEdu;
        this.props = props;
    }

    /** 幂等返回：同一幂等键重复提交直接返回首次结果。 */
    public Optional<PointTransaction> findByIdempotent(String key) {
        return transactionRepository.findByIdempotentKey(key);
    }

    @Transactional
    public Map<String, Object> grant(String userId, String sessionId, String cefrLevel,
                                     int repeatCount, Assertion assertion, String idempotentKey) {
        // 1. 幂等兜底
        Optional<PointTransaction> existing = transactionRepository.findByIdempotentKey(idempotentKey);
        if (existing.isPresent()) return toGrantResult(existing.get(), "重复提交，返回首次结果");

        // 2. 断言签名 / 时间窗校验（防重放、防跨用户伪造）
        if (assertion == null || !omniEdu.verifyAssertion(assertion)) {
            throw new BizException(401, "学习断言校验失败，不予发放积分");
        }
        if (assertion.qualityScore() < props.getQuality().getZeroMax()) {
            throw new BizException(400, "质量系数低于 0.4，未达到积分发放阈值");
        }

        // 3. 公式结算
        Map<String, Object> calc = computeReward(cefrLevel, assertion.qualityScore(), repeatCount);
        long computed = (long) calc.get("computed");

        LearningAccount account = ensureAccount(userId);
        resetDailyIfNewDay(account);
        long cap = account.isMember() ? props.getDailyCapMember() : props.getDailyCapFree();
        long remain = Math.max(0, cap - account.getTodayEarned());
        long granted = Math.min(computed, remain);
        boolean capped = granted < computed;

        if (granted <= 0) {
            throw new BizException(400, "今日积分获取已达上限，超出部分不予发放");
        }

        // 4. 乐观锁写入（冲突重试）
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                account.setBalance(account.getBalance() + granted);
                account.setTotalEarned(account.getTotalEarned() + granted);
                account.setTodayEarned(account.getTodayEarned() + granted);
                accountRepository.save(account);
                break;
            } catch (OptimisticLockingFailureException e) {
                if (attempt == MAX_RETRY) throw new BizException(409, "账户并发冲突，请重试");
                reload(account);
            }
        }

        // 5. 写流水（CREDIT 用户收入）
        String txNo = "TX" + System.currentTimeMillis() + (int) (Math.random() * 1000);
        String leaf = evidenceService.leafHash(txNo, "GRANT", granted, sessionId, System.currentTimeMillis());
        PointTransaction tx = new PointTransaction();
        tx.setTxNo(txNo);
        tx.setIdempotentKey(idempotentKey);
        tx.setUserId(userId);
        tx.setBizType("GRANT");
        tx.setDirection("CREDIT");
        tx.setAmount(granted);
        tx.setBalanceAfter(account.getBalance());
        tx.setSourceBiz(sessionId);
        tx.setEvidenceHash(leaf);
        transactionRepository.save(tx);

        // 6. 异步存证（不阻塞响应）
        evidenceService.submitLeaf(leaf);

        Map<String, Object> result = new HashMap<>();
        result.put("granted", granted);
        result.put("computed", computed);
        result.put("capped", capped);
        result.put("balance", account.getBalance());
        result.put("txNo", txNo);
        result.put("evidenceHash", leaf);
        result.put("cefrCoeff", calc.get("cefrCoeff"));
        result.put("qualityCoeff", calc.get("qualityCoeff"));
        result.put("repeatCoeff", calc.get("repeatCoeff"));
        result.put("base", props.getBaseReward());
        result.put("message", capped ? "已达单日上限，部分发放" : "发放成功");
        return result;
    }

    /** 积分兑换：余额校验、扣减与权益发放同事务，幂等键兜底。 */
    @Transactional
    public Map<String, Object> redeem(String userId, String itemId, long costPoints,
                                      String itemName, String idempotentKey) {
        Optional<PointTransaction> existing = transactionRepository.findByIdempotentKey(idempotentKey);
        if (existing.isPresent()) return Map.of("idempotent", true, "txNo", existing.get().getTxNo(),
                "balance", existing.get().getBalanceAfter());

        LearningAccount account = ensureAccount(userId);
        if (!"ACTIVE".equals(account.getStatus())) throw new BizException(400, "账户状态异常，无法兑换");
        if (account.getBalance() < costPoints) {
            throw new BizException(400, "积分余额不足，可选择人民币直接购买");
        }

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                account.setBalance(account.getBalance() - costPoints);
                account.setTotalSpent(account.getTotalSpent() + costPoints);
                accountRepository.save(account);
                break;
            } catch (OptimisticLockingFailureException e) {
                if (attempt == MAX_RETRY) throw new BizException(409, "账户并发冲突，请重试");
                reload(account);
            }
        }

        String txNo = "TX" + System.currentTimeMillis() + (int) (Math.random() * 1000);
        String leaf = evidenceService.leafHash(txNo, "REDEEM", costPoints, itemId, System.currentTimeMillis());
        PointTransaction tx = new PointTransaction();
        tx.setTxNo(txNo);
        tx.setIdempotentKey(idempotentKey);
        tx.setUserId(userId);
        tx.setBizType("REDEEM");
        tx.setDirection("DEBIT");
        tx.setAmount(costPoints);
        tx.setBalanceAfter(account.getBalance());
        tx.setSourceBiz(itemId);
        tx.setEvidenceHash(leaf);
        transactionRepository.save(tx);
        evidenceService.submitLeaf(leaf);

        Map<String, Object> result = new HashMap<>();
        result.put("idempotent", false);
        result.put("txNo", txNo);
        result.put("balance", account.getBalance());
        result.put("spent", costPoints);
        result.put("item", itemName);
        result.put("evidenceHash", leaf);
        result.put("message", "兑换成功，已消耗积分不予退还");
        return result;
    }

    /** 学分购买（P0 mock 收银台，不接真实支付）：CREDIT 入账，幂等，不受学习单日上限约束。 */
    @Transactional
    public Map<String, Object> topUp(String userId, long amount, String packageId, String idempotentKey) {
        Optional<PointTransaction> existing = transactionRepository.findByIdempotentKey(idempotentKey);
        if (existing.isPresent()) return Map.of("idempotent", true, "txNo", existing.get().getTxNo(),
                "balance", existing.get().getBalanceAfter(), "amount", existing.get().getAmount());
        if (amount <= 0) throw new BizException(400, "充值金额必须大于 0");

        LearningAccount account = ensureAccount(userId);
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                account.setBalance(account.getBalance() + amount);
                account.setTotalEarned(account.getTotalEarned() + amount);
                accountRepository.save(account);
                break;
            } catch (OptimisticLockingFailureException e) {
                if (attempt == MAX_RETRY) throw new BizException(409, "账户并发冲突，请重试");
                reload(account);
            }
        }

        String txNo = "TX" + System.currentTimeMillis() + (int) (Math.random() * 1000);
        String leaf = evidenceService.leafHash(txNo, "TOPUP", amount, packageId, System.currentTimeMillis());
        PointTransaction tx = new PointTransaction();
        tx.setTxNo(txNo);
        tx.setIdempotentKey(idempotentKey);
        tx.setUserId(userId);
        tx.setBizType("TOPUP");
        tx.setDirection("CREDIT");
        tx.setAmount(amount);
        tx.setBalanceAfter(account.getBalance());
        tx.setSourceBiz(packageId);
        tx.setEvidenceHash(leaf);
        transactionRepository.save(tx);
        evidenceService.submitLeaf(leaf);

        Map<String, Object> result = new HashMap<>();
        result.put("idempotent", false);
        result.put("txNo", txNo);
        result.put("balance", account.getBalance());
        result.put("amount", amount);
        result.put("packageId", packageId);
        result.put("evidenceHash", leaf);
        result.put("message", "mock 演示支付成功，学分已到账（不发生真实扣款）");
        return result;
    }

    public Page<PointTransaction> ledger(String userId, int page, int size) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
    }

    public LearningAccount accountInfo(String userId) {
        return ensureAccount(userId);
    }

    /** 公式结算：基础 × CEFR × 质量 × 重复衰减。 */
    public Map<String, Object> computeReward(String cefr, double quality, int repeatCount) {
        double cefrCoeff = Optional.ofNullable(props.getCefr().get(cefr)).orElse(1.0);
        double qualityCoeff;
        PointsProperties.Quality q = props.getQuality();
        if (quality < q.getZeroMax()) qualityCoeff = q.getZero();
        else if (quality < q.getMidMax()) qualityCoeff = q.getMid();
        else if (quality < q.getHighMax()) qualityCoeff = q.getHigh();
        else qualityCoeff = q.getTop();
        double repeatCoeff = repeatCount > 0 ? props.getRepeat().getReview() : props.getRepeat().getFirst();

        double raw = props.getBaseReward() * cefrCoeff * qualityCoeff * repeatCoeff;
        long computed = Math.max(0, Math.round(raw));
        Map<String, Object> m = new HashMap<>();
        m.put("cefrCoeff", cefrCoeff);
        m.put("qualityCoeff", qualityCoeff);
        m.put("repeatCoeff", repeatCoeff);
        m.put("computed", computed);
        return m;
    }

    private LearningAccount ensureAccount(String userId) {
        LearningAccount account = accountRepository.findByUserId(userId).orElse(null);
        if (account == null) {
            account = new LearningAccount();
            account.setUserId(userId);
            account.setBalance(0);
            account.setFrozen(0);
            account.setTotalEarned(0);
            account.setTotalSpent(0);
            account.setStatus("ACTIVE");
            account.setMember(false);
            account.setTodayEarned(0);
            account.setTodayDate(LocalDate.now().toString());
            account.setValidUntil(LocalDateTime.now().plusYears(1));
            account = accountRepository.save(account);
        }
        return account;
    }

    private void resetDailyIfNewDay(LearningAccount account) {
        String today = LocalDate.now().toString();
        if (!today.equals(account.getTodayDate())) {
            account.setTodayDate(today);
            account.setTodayEarned(0);
        }
    }

    private void reload(LearningAccount account) {
        LearningAccount fresh = accountRepository.findByUserId(account.getUserId())
                .orElseThrow(() -> new BizException(500, "账户不存在"));
        account.setBalance(fresh.getBalance());
        account.setVersion(fresh.getVersion());
        account.setTodayEarned(fresh.getTodayEarned());
        account.setTotalEarned(fresh.getTotalEarned());
        account.setTotalSpent(fresh.getTotalSpent());
    }

    private Map<String, Object> toGrantResult(PointTransaction tx, String message) {
        Map<String, Object> r = new HashMap<>();
        r.put("granted", tx.getAmount());
        r.put("balance", tx.getBalanceAfter());
        r.put("txNo", tx.getTxNo());
        r.put("evidenceHash", tx.getEvidenceHash());
        r.put("message", message);
        return r;
    }
}
