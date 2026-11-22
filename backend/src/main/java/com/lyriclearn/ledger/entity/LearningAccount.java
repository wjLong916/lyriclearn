package com.lyriclearn.ledger.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 学习积分账户表（PRD 表11 / 架构 8.2）。中心化账本，不映射链上余额；
 * 余额变更采用乐观锁版本号，避免并发覆盖。
 */
@Entity
@Table(name = "learning_account",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id"}))
public class LearningAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    /** 当前可用积分余额 */
    @Column(nullable = false)
    private long balance;

    /** 冻结积分 */
    @Column(nullable = false)
    private long frozen;

    @Column(name = "total_earned", nullable = false)
    private long totalEarned;

    @Column(name = "total_spent", nullable = false)
    private long totalSpent;

    /** 积分有效期截止 */
    @Column(name = "valid_until")
    private LocalDateTime validUntil;

    /** 账户状态：ACTIVE / FROZEN */
    @Column(length = 16)
    private String status = "ACTIVE";

    /** 是否付费会员（影响单日额度与语音纠偏） */
    @Column(nullable = false)
    private boolean member;

    /** 今日已获取积分（额度硬截断） */
    @Column(name = "today_earned", nullable = false)
    private long todayEarned;

    @Column(name = "today_date", length = 10)
    private String todayDate;

    /** 乐观锁版本号 */
    @Version
    private Integer version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }
    public long getFrozen() { return frozen; }
    public void setFrozen(long frozen) { this.frozen = frozen; }
    public long getTotalEarned() { return totalEarned; }
    public void setTotalEarned(long totalEarned) { this.totalEarned = totalEarned; }
    public long getTotalSpent() { return totalSpent; }
    public void setTotalSpent(long totalSpent) { this.totalSpent = totalSpent; }
    public LocalDateTime getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDateTime validUntil) { this.validUntil = validUntil; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isMember() { return member; }
    public void setMember(boolean member) { this.member = member; }
    public long getTodayEarned() { return todayEarned; }
    public void setTodayEarned(long todayEarned) { this.todayEarned = todayEarned; }
    public String getTodayDate() { return todayDate; }
    public void setTodayDate(String todayDate) { this.todayDate = todayDate; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
}
