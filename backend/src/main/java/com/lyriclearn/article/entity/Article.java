package com.lyriclearn.article.entity;

import jakarta.persistence.*;

/**
 * 自建英文文章库（PRD 扩展：英文文章学习）。
 * 级别与歌曲一致（CEFR A1-C1 / L0-L4），分类 NEWS 新闻 / SPEECH 名人演讲稿。
 * 仅存平台原创或公有领域文本与增强标注，不复制受版权保护的原文。
 */
@Entity
@Table(name = "article",
        uniqueConstraints = @UniqueConstraint(columnNames = {"article_id"}))
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false, length = 64)
    private String articleId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 128)
    private String author;

    /** 来源/出处（原创标注或公有领域篇目） */
    @Column(length = 160)
    private String source;

    /** NEWS 新闻 / SPEECH 演讲稿 */
    @Column(length = 16)
    private String category;

    /** CEFR 等级 A1/A2/B1/B2/C1 */
    @Column(length = 8)
    private String cefr;

    /** 平台难度层级 L0-L4 */
    @Column(name = "level", length = 8)
    private String level;

    @Column(length = 400)
    private String summary;

    /** 正文段落，JSON 字符串数组 */
    @Column(name = "content_json", length = 8000)
    private String contentJson;

    /** 生词标注 JSON：word/phonetic/meaning */
    @Column(name = "vocab_json", length = 4000)
    private String vocabJson;

    @Column(length = 16)
    private String reviewStatus = "ONLINE";

    /**
     * 呈现模式：FULL_TEXT 站内全文（原创/公有领域）；
     * SUMMARY_LINK 仅平台摘要 + 原文外链跳转（受版权保护素材，不落原文）。
     */
    @Column(length = 16)
    private String accessMode = "FULL_TEXT";

    /** 原文外链（SUMMARY_LINK 模式必填，新标签页跳转第三方，本站不存储原文） */
    @Column(length = 500)
    private String sourceUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getArticleId() { return articleId; }
    public void setArticleId(String articleId) { this.articleId = articleId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCefr() { return cefr; }
    public void setCefr(String cefr) { this.cefr = cefr; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public String getVocabJson() { return vocabJson; }
    public void setVocabJson(String vocabJson) { this.vocabJson = vocabJson; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    public String getAccessMode() { return accessMode; }
    public void setAccessMode(String accessMode) { this.accessMode = accessMode; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}
