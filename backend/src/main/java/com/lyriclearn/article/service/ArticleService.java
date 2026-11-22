package com.lyriclearn.article.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lyriclearn.article.entity.Article;
import com.lyriclearn.article.repo.ArticleRepository;
import com.lyriclearn.common.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 文章库服务：列表筛选与详情（正文段落 + 生词标注）。 */
@Service
public class ArticleService {

    private final ArticleRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    public ArticleService(ArticleRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> list(String level, String category, String keyword) {
        List<Article> all = repository.findByReviewStatus("ONLINE");
        return all.stream()
                .filter(a -> level == null || level.isBlank() || a.getCefr().equals(level))
                .filter(a -> category == null || category.isBlank() || a.getCategory().equals(category))
                .filter(a -> keyword == null || keyword.isBlank()
                        || a.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public Map<String, Object> detail(String articleId) {
        Article a = repository.findByArticleId(articleId)
                .orElseThrow(() -> new BizException(404, "文章不存在：" + articleId));
        Map<String, Object> m = toSummary(a);
        // SUMMARY_LINK（受版权保护素材）不下发原文段落，仅平台摘要 + 生词 + 外链；
        // FULL_TEXT（原创/公有领域）下发站内全文。
        if (!"SUMMARY_LINK".equals(a.getAccessMode())) {
            m.put("paragraphs", parseList(a.getContentJson()));
        }
        m.put("vocab", parseList(a.getVocabJson()));
        return m;
    }

    private Map<String, Object> toSummary(Article a) {
        Map<String, Object> m = new java.util.HashMap<>();
        m.put("articleId", a.getArticleId());
        m.put("title", a.getTitle());
        m.put("author", a.getAuthor());
        m.put("source", a.getSource());
        m.put("category", a.getCategory());
        m.put("cefr", a.getCefr());
        m.put("level", a.getLevel());
        m.put("summary", a.getSummary());
        m.put("accessMode", a.getAccessMode());
        m.put("sourceUrl", a.getSourceUrl());
        return m;
    }

    private List<Map<String, Object>> parseList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
