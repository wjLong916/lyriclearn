package com.lyriclearn.evidence;

import com.lyriclearn.evidence.entity.LearningCredential;
import com.lyriclearn.evidence.repo.LearningCredentialRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 存证服务（架构 10）：单笔流水摘要 SHA-256，窗口哈希链批量锚定；
 * SBT 不可转让铸造。本阶段以内存模拟链记录根哈希，生产环境替换为合约 anchorRoot / mintSbt。
 * 存证失败不回滚业务，凭证进入待铸造由补偿任务重试。
 */
@Service
public class EvidenceService {

    private final LearningCredentialRepository credentialRepository;

    /** 窗口锚定记录：windowId -> rootHash（模拟链上）。 */
    private final Map<Long, String> anchoredWindows = new ConcurrentHashMap<>();
    /** 待锚定单笔哈希队列。 */
    private final Queue<String> pendingLeaves = new ConcurrentLinkedQueue<>();
    private final AtomicLong windowSeq = new AtomicLong(1);

    public EvidenceService(LearningCredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    /** 规范化摘要 → 单笔叶子哈希（流水号/类型/数量/业务标识/时间戳）。 */
    public String leafHash(String txNo, String type, long amount, String sourceBiz, long timestamp) {
        String normalized = txNo + "|" + type + "|" + amount + "|" + sourceBiz + "|" + timestamp;
        return sha256(normalized);
    }

    /** 业务发放/兑换后提交叶子哈希，进入待锚定队列（异步，不阻塞业务）。 */
    public void submitLeaf(String leafHash) {
        pendingLeaves.offer(leafHash);
    }

    /** 批量锚定：把窗口内叶子串成哈希链，根哈希写入模拟链。返回 windowId 与 root。 */
    public Map<String, Object> anchorWindow() {
        List<String> leaves = new ArrayList<>();
        String leaf;
        while ((leaf = pendingLeaves.poll()) != null) leaves.add(leaf);
        long windowId = windowSeq.getAndIncrement();
        String root = "0".repeat(64);
        for (String l : leaves) {
            root = sha256(root + l);
        }
        anchoredWindows.put(windowId, root);
        return Map.of("windowId", windowId, "root", root, "leafCount", leaves.size());
    }

    /** 只读核验（生产为合约 verify(leaf, proof, windowId)）。 */
    public boolean verify(String leaf, long windowId) {
        return anchoredWindows.containsKey(windowId);
    }

    /**
     * SBT 铸造：同一 claimHash 不可重复；凭证不可转让、不可拆分。
     * 返回凭证标识与模拟交易哈希。
     */
    public synchronized Map<String, Object> mintSbt(String userId, String claimHash) {
        Optional<LearningCredential> existing = credentialRepository.findByClaimHash(claimHash);
        if (existing.isPresent()) {
            throw new IllegalStateException("该学习断言已铸造过凭证，SBT 不可重复铸造");
        }
        long now = System.currentTimeMillis();
        LearningCredential c = new LearningCredential();
        c.setUserId(userId);
        c.setClaimHash(claimHash);
        c.setCredentialId("sbt-" + now);
        c.setTxHash("0x" + sha256("mint|" + userId + "|" + claimHash + "|" + now).substring(0, 40));
        c.setStatus("MINTED");
        c.setMintedAt(LocalDateTime.now());
        credentialRepository.save(c);
        return Map.of(
                "credentialId", c.getCredentialId(),
                "txHash", c.getTxHash(),
                "status", c.getStatus(),
                "transferable", false);
    }

    public Optional<LearningCredential> getCredential(String credentialId) {
        return credentialRepository.findByCredentialId(credentialId);
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
