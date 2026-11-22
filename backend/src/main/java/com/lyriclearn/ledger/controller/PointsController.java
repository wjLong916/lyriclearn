package com.lyriclearn.ledger.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.common.BizException;
import com.lyriclearn.ledger.entity.LearningAccount;
import com.lyriclearn.ledger.entity.PointTransaction;
import com.lyriclearn.ledger.model.RewardCatalog;
import com.lyriclearn.ledger.model.RewardItem;
import com.lyriclearn.ledger.service.PointLedgerService;
import com.lyriclearn.omniedu.OmniEduDto.Assertion;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 积分账本：发放、兑换、账户与账本。 */
@RestController
@RequestMapping("/api/points")
public class PointsController {

    private final PointLedgerService ledger;
    private final RewardCatalog catalog;

    public PointsController(PointLedgerService ledger, RewardCatalog catalog) {
        this.ledger = ledger;
        this.catalog = catalog;
    }

    /** 积分发放：校验断言→公式结算→额度截断→入账→异步存证。 */
    @PostMapping("/grant")
    public ApiResponse<Map<String, Object>> grant(@RequestBody GrantRequest req) {
        Assertion assertion = new Assertion(
                req.assertionId(), req.songId(), req.snapshotVersion(),
                req.qualityScore(), req.masteredWords(), req.signedAt(), req.signature());
        return ApiResponse.ok(ledger.grant(req.userId(), req.sessionId(), req.cefr(),
                req.repeatCount() == null ? 0 : req.repeatCount(), assertion, req.idempotentKey()));
    }

    /** 积分兑换：余额校验、扣减与权益发放同事务。 */
    @PostMapping("/redeem")
    public ApiResponse<Map<String, Object>> redeem(@RequestBody RedeemRequest req) {
        RewardItem item = catalog.byId(req.itemId())
                .orElseThrow(() -> new BizException(404, "权益不存在：" + req.itemId()));
        return ApiResponse.ok(ledger.redeem(req.userId(), item.itemId(), item.costPoints(),
                item.name(), req.idempotentKey()));
    }

    /** 学分购买档位（mock 演示，不接真实支付）。 */
    @GetMapping("/packages")
    public ApiResponse<List<Map<String, Object>>> packages() {
        return ApiResponse.ok(List.of(
                Map.of("packageId", "pkg-100", "credits", 100, "priceLabel", "10 元（mock）"),
                Map.of("packageId", "pkg-300", "credits", 300, "priceLabel", "28 元（mock）"),
                Map.of("packageId", "pkg-600", "credits", 600, "priceLabel", "52 元（mock）")));
    }

    /** 学分购买（mock 收银台）：CREDIT 入账，幂等。 */
    @PostMapping("/top-up")
    public ApiResponse<Map<String, Object>> topUp(@RequestBody TopUpRequest req) {
        long credits = switch (req.packageId()) {
            case "pkg-100" -> 100;
            case "pkg-300" -> 300;
            case "pkg-600" -> 600;
            default -> throw new BizException(404, "档位不存在：" + req.packageId());
        };
        return ApiResponse.ok(ledger.topUp(req.userId(), credits, req.packageId(), req.idempotentKey()));
    }

    /** 账户信息：余额、累计、今日已获与单日上限。 */
    @GetMapping("/account")
    public ApiResponse<Map<String, Object>> account(@RequestParam String userId) {
        LearningAccount a = ledger.accountInfo(userId);
        long cap = a.isMember() ? 100 : 30;
        long today = LocalDate.now().toString().equals(a.getTodayDate()) ? a.getTodayEarned() : 0;
        Map<String, Object> r = Map.of(
                "userId", a.getUserId(),
                "balance", a.getBalance(),
                "totalEarned", a.getTotalEarned(),
                "totalSpent", a.getTotalSpent(),
                "todayEarned", today,
                "dailyCap", cap,
                "member", a.isMember(),
                "validUntil", a.getValidUntil() == null ? "" : a.getValidUntil().toString(),
                "status", a.getStatus());
        return ApiResponse.ok(r);
    }

    /** 积分账本：分页收支明细与存证哈希。 */
    @GetMapping("/ledger")
    public ApiResponse<Map<String, Object>> ledger(@RequestParam String userId,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        Page<PointTransaction> p = ledger.ledger(userId, page, size);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (PointTransaction t : p.getContent()) {
            rows.add(Map.of(
                    "txNo", t.getTxNo(),
                    "bizType", t.getBizType(),
                    "direction", t.getDirection(),
                    "amount", t.getAmount(),
                    "balanceAfter", t.getBalanceAfter(),
                    "sourceBiz", t.getSourceBiz() == null ? "" : t.getSourceBiz(),
                    "evidenceHash", t.getEvidenceHash() == null ? "" : t.getEvidenceHash(),
                    "createdAt", t.getCreatedAt().toString()));
        }
        return ApiResponse.ok(Map.of(
                "rows", rows,
                "total", p.getTotalElements(),
                "page", page,
                "size", size));
    }

    /** 权益目录（商城）。 */
    @GetMapping("/items")
    public ApiResponse<List<RewardItem>> items() {
        return ApiResponse.ok(catalog.all());
    }

    public record GrantRequest(String userId, String sessionId, String cefr, Integer repeatCount,
                               String assertionId, String songId, String snapshotVersion,
                               double qualityScore, List<String> masteredWords,
                               long signedAt, String signature, String idempotentKey) {}

    public record RedeemRequest(String userId, String itemId, String idempotentKey) {}

    public record TopUpRequest(String userId, String packageId, String idempotentKey) {}
}
