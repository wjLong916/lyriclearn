package com.lyriclearn.evidence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 学习凭证表（PRD 表11 / 架构 8.2）。SBT 与账户绑定、不可转让；
 * 链上仅记录断言摘要与凭证标识，不记录歌词、音频与个人明细。
 */
@Entity
@Table(name = "learning_credential",
        uniqueConstraints = @UniqueConstraint(columnNames = {"claim_hash"}))
public class LearningCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /** 中台断言摘要哈希（同一 claimHash 不可重复铸造） */
    @Column(name = "claim_hash", nullable = false, length = 64)
    private String claimHash;

    @Column(name = "credential_id", length = 64)
    private String credentialId;

    @Column(name = "tx_hash", length = 64)
    private String txHash;

    /** PENDING / MINTED */
    @Column(length = 16)
    private String status = "PENDING";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "minted_at")
    private LocalDateTime mintedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getClaimHash() { return claimHash; }
    public void setClaimHash(String claimHash) { this.claimHash = claimHash; }
    public String getCredentialId() { return credentialId; }
    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
    public String getTxHash() { return txHash; }
    public void setTxHash(String txHash) { this.txHash = txHash; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getMintedAt() { return mintedAt; }
    public void setMintedAt(LocalDateTime mintedAt) { this.mintedAt = mintedAt; }
}
