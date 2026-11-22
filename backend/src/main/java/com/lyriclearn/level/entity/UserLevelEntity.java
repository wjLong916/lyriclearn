package com.lyriclearn.level.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** 用户当前 CEFR 级别：入学测试(PLACEMENT)或按成绩自动评定(EVALUATED)。 */
@Entity
@Table(name = "user_level",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
public class UserLevelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Column(length = 8)
    private String cefr;

    /** PLACEMENT 入学测试 / EVALUATED 按成绩评定 */
    @Column(length = 16)
    private String source;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getCefr() { return cefr; }
    public void setCefr(String cefr) { this.cefr = cefr; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
