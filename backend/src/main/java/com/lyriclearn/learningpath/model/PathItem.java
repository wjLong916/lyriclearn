package com.lyriclearn.learningpath.model;

/**
 * 路径内单个学习资源。
 * type: SONG 歌曲 / ARTICLE 文章（新闻/演讲稿/诗歌）；
 * ref: SONG 为 platform:songId，ARTICLE 为 articleId。
 */
public record PathItem(String type, String ref, String title, String cefr) {
}
