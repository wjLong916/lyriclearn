package com.lyriclearn.annotation.entity;

import jakarta.persistence.*;

/**
 * 歌曲学习标注表（对应 PRD 表11 / 架构 8.2）。
 * 平台标识 + 歌曲标识 联合唯一；只存学习增强标注与公开元信息，
 * 严禁持久化原版歌词（合规技术约束 13.4）。
 */
@Entity
@Table(name = "song_annotation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"platform", "song_id"}))
public class SongAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 第三方平台标识（mock：qqmusic / netease） */
    @Column(nullable = false, length = 32)
    private String platform;

    @Column(name = "song_id", nullable = false, length = 64)
    private String songId;

    /** 歌名（公开元信息，可缓存） */
    @Column(nullable = false, length = 128)
    private String title;

    @Column(nullable = false, length = 128)
    private String artist;

    @Column(length = 256)
    private String cover;

    /** 平台难度层级 L0-L4 */
    @Column(name = "difficulty_level", length = 8)
    private String difficultyLevel;

    /** CEFR 等级 A1/A2/B1/B2/C1 */
    @Column(name = "cefr_level", length = 8)
    private String cefrLevel;

    /** 标注结构体（句子级时间轴 + 词级标注），JSON */
    @Column(name = "annotation_json", length = 8000)
    private String annotationJson;

    /** 标注版本，版本升级触发缓存失效 */
    @Column(name = "version")
    private Integer version = 1;

    /** 审核状态：ONLINE / OFFLINE */
    @Column(length = 16)
    private String reviewStatus = "ONLINE";

    /** 是否可学习（标注库中已上线才可刻意学习） */
    @Column(name = "learnable")
    private boolean learnable = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getSongId() { return songId; }
    public void setSongId(String songId) { this.songId = songId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }
    public String getCover() { return cover; }
    public void setCover(String cover) { this.cover = cover; }
    public String getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public String getCefrLevel() { return cefrLevel; }
    public void setCefrLevel(String cefrLevel) { this.cefrLevel = cefrLevel; }
    public String getAnnotationJson() { return annotationJson; }
    public void setAnnotationJson(String annotationJson) { this.annotationJson = annotationJson; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public boolean isLearnable() { return learnable; }
    public void setLearnable(boolean learnable) { this.learnable = learnable; }
}
