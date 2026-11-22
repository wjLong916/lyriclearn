package com.lyriclearn.learningpath.entity;

import jakarta.persistence.*;

/** 路径内单个资源：SONG 歌曲 / ARTICLE 文章（新闻/演讲稿/诗歌）。 */
@Entity
@Table(name = "learning_path_item")
public class LearningPathItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "path_id", nullable = false, length = 64)
    private String pathId;

    /** SONG / ARTICLE */
    @Column(length = 16)
    private String itemType;

    /** SONG 为 platform:songId，ARTICLE 为 articleId */
    @Column(length = 128)
    private String ref;

    @Column(length = 160)
    private String title;

    @Column(length = 8)
    private String cefr;

    @Column(name = "seq")
    private int seq;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPathId() { return pathId; }
    public void setPathId(String pathId) { this.pathId = pathId; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCefr() { return cefr; }
    public void setCefr(String cefr) { this.cefr = cefr; }
    public int getSeq() { return seq; }
    public void setSeq(int seq) { this.seq = seq; }
}
