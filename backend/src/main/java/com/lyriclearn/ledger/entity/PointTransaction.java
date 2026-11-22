package com.lyriclearn.ledger.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 积分流水表（复式记账分录，架构 9.1）。流水号全局唯一，幂等键唯一索引兜底；
 * 按月分区（生产），禁止物理删除，凭 evidenceHash 可链上核验。
 */
@Entity
@Table(name = "point_transaction",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"tx_no"}),
                @UniqueConstraint(columnNames = {"idempotent_key"})
        })
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 全局流水号 */
    @Column(name = "tx_no", nullable = false, length = 64)
    private String txNo;

    /** 幂等键：用户+任务+会话组合，重复提交返回首次结果 */
    @Column(name = "idempotent_key", nullable = false, length = 128)
    private String idempotentKey;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /** 业务类型：GRANT 学习发放 / REDEEM 兑换消耗 / REVIEW 复习 / ACHIEVE 成就 */
    @Column(name = "biz_type", nullable = false, length = 16)
    private String bizType;

    /** 方向：CREDIT 用户收入 / DEBIT 用户支出 */
    @Column(length = 8)
    private String direction;

    @Column(nullable = false)
    private long amount;

    @Column(name = "balance_after")
    private long balanceAfter;

    /** 来源业务标识（会话/权益） */
    @Column(name = "source_biz", length = 128)
    private String sourceBiz;

    /** 链上存证哈希（单笔摘要） */
    @Column(name = "evidence_hash", length = 64)
    private String evidenceHash;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTxNo() { return txNo; }
    public void setTxNo(String txNo) { this.txNo = txNo; }
    public String getIdempotentKey() { return idempotentKey; }
    public void setIdempotentKey(String idempotentKey) { this.idempotentKey = idempotentKey; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public long getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(long balanceAfter) { this.balanceAfter = balanceAfter; }
    public String getSourceBiz() { return sourceBiz; }
    public void setSourceBiz(String sourceBiz) { this.sourceBiz = sourceBiz; }
    public String getEvidenceHash() { return evidenceHash; }
    public void setEvidenceHash(String evidenceHash) { this.evidenceHash = evidenceHash; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
