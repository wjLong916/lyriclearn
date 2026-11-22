package com.lyriclearn.learningpath.entity;

import jakarta.persistence.*;

/** 混合学习路径（管理员可编排、定价、上下架）。 */
@Entity
@Table(name = "learning_path",
        uniqueConstraints = @UniqueConstraint(columnNames = "path_id"))
public class LearningPathEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path_id", nullable = false, length = 64)
    private String pathId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 8)
    private String cefr;

    @Column(length = 8)
    private String level;

    @Column(name = "price_credits")
    private int priceCredits;

    @Column(length = 8)
    private String published = "ON";

    @Column(name = "sort_order")
    private int sortOrder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPathId() { return pathId; }
    public void setPathId(String pathId) { this.pathId = pathId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCefr() { return cefr; }
    public void setCefr(String cefr) { this.cefr = cefr; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public int getPriceCredits() { return priceCredits; }
    public void setPriceCredits(int priceCredits) { this.priceCredits = priceCredits; }
    public String getPublished() { return published; }
    public void setPublished(String published) { this.published = published; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
