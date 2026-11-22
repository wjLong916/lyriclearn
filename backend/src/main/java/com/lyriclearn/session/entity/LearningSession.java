package com.lyriclearn.session.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 学习会话表（架构 8.2）。以会话为单位记录五阶段状态机与学习结果；
 * 阶段只允许单向推进：PREVIEW → LISTEN → FOLLOW → PRACTICE → REVIEW。
 */
@Entity
@Table(name = "learning_session",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id"}))
public class LearningSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(length = 32)
    private String platform;

    @Column(name = "song_id", length = 64)
    private String songId;

    /** 学习模式：FOCUS 刻意学习 / CASUAL 休闲听歌 */
    @Column(length = 16)
    private String mode = "FOCUS";

    /** 阶段：PREVIEW/LISTEN/FOLLOW/PRACTICE/REVIEW */
    @Column(length = 16)
    private String stage = "PREVIEW";

    /** 状态：IN_PROGRESS / FINISHED */
    @Column(length = 16)
    private String status = "IN_PROGRESS";

    @Column(name = "quality_score")
    private Double qualityScore;

    @Column(name = "cefr_level", length = 8)
    private String cefrLevel;

    /** 重复学习次数（用于衰减系数） */
    @Column(name = "repeat_count")
    private Integer repeatCount = 0;

    /** 中台断言 JSON（仅签名摘要，不存歌词） */
    @Column(name = "assertion_json", length = 2000)
    private String assertionJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStage() { return stage; }
    public void setStage(String stage) { this.stage = stage; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getQualityScore() { return qualityScore; }
    public void setQualityScore(Double qualityScore) { this.qualityScore = qualityScore; }
    public String getCefrLevel() { return cefrLevel; }
    public void setCefrLevel(String cefrLevel) { this.cefrLevel = cefrLevel; }
    public Integer getRepeatCount() { return repeatCount; }
    public void setRepeatCount(Integer repeatCount) { this.repeatCount = repeatCount; }
    public String getAssertionJson() { return assertionJson; }
    public void setAssertionJson(String assertionJson) { this.assertionJson = assertionJson; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getFinishedAt() { return finishedAt; }
    public void setFinishedAt(LocalDateTime finishedAt) { this.finishedAt = finishedAt; }
}
