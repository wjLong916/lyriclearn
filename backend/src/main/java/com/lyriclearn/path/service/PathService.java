package com.lyriclearn.path.service;

import com.lyriclearn.article.entity.Article;
import com.lyriclearn.article.repo.ArticleRepository;
import com.lyriclearn.session.entity.LearningSession;
import com.lyriclearn.session.repo.LearningSessionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 学习路径服务：L0–L4 线性关卡。
 * 一级全部篇目质量分达标（≥0.7）后解锁下一级；达标成绩取自已完成会话（FINISHED）的最高质量分。
 */
@Service
public class PathService {

    /** 达标线：质量分 ≥ 0.7 视为该篇目掌握 */
    private static final double PASS_SCORE = 0.7;
    private static final List<String> LEVEL_ORDER = List.of("L0", "L1", "L2", "L3", "L4");

    private final ArticleRepository articleRepository;
    private final LearningSessionRepository sessionRepository;

    public PathService(ArticleRepository articleRepository,
                       LearningSessionRepository sessionRepository) {
        this.articleRepository = articleRepository;
        this.sessionRepository = sessionRepository;
    }

    public Map<String, Object> path(String userId) {
        // 该用户每篇素材的最高完成质量分（按 songId/articleId 聚合 FINISHED 会话）
        Map<String, Double> best = new HashMap<>();
        for (LearningSession s : sessionRepository.findByUserIdAndStatus(userId, "FINISHED")) {
            if (s.getSongId() == null) continue;
            double q = s.getQualityScore() == null ? 0 : s.getQualityScore();
            best.merge(s.getSongId(), q, Math::max);
        }

        List<Article> all = articleRepository.findByReviewStatus("ONLINE");
        Map<String, List<Article>> byLevel = all.stream()
                .filter(a -> a.getLevel() != null && LEVEL_ORDER.contains(a.getLevel()))
                .collect(Collectors.groupingBy(Article::getLevel, LinkedHashMap::new, Collectors.toList()));

        List<Map<String, Object>> stages = new ArrayList<>();
        boolean prevPassed = true; // L0 默认解锁
        for (String level : LEVEL_ORDER) {
            List<Article> items = byLevel.getOrDefault(level, List.of());
            boolean levelUnlocked = prevPassed;
            List<Map<String, Object>> rows = new ArrayList<>();
            boolean allPassed = !items.isEmpty();
            for (Article a : items) {
                double score = best.getOrDefault(a.getArticleId(), 0.0);
                boolean done = score >= PASS_SCORE;
                allPassed = allPassed && done;
                String status = done ? "done" : (levelUnlocked ? "available" : "locked");
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("articleId", a.getArticleId());
                row.put("title", a.getTitle());
                row.put("category", a.getCategory());
                row.put("cefr", a.getCefr());
                row.put("level", a.getLevel());
                row.put("bestScore", Math.round(score * 100) / 100.0);
                row.put("status", status);
                rows.add(row);
            }
            Map<String, Object> stage = new LinkedHashMap<>();
            stage.put("level", level);
            stage.put("unlocked", levelUnlocked);
            stage.put("allPassed", allPassed);
            stage.put("items", rows);
            stages.add(stage);
            prevPassed = allPassed;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", userId);
        result.put("passScore", PASS_SCORE);
        result.put("stages", stages);
        return result;
    }
}
