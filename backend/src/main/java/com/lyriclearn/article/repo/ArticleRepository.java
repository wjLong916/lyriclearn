package com.lyriclearn.article.repo;

import com.lyriclearn.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findByArticleId(String articleId);

    List<Article> findByReviewStatus(String reviewStatus);

    List<Article> findByCategoryAndReviewStatus(String category, String reviewStatus);
}
