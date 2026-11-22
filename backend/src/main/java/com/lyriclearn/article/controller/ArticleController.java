package com.lyriclearn.article.controller;

import com.lyriclearn.article.service.ArticleService;
import com.lyriclearn.common.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 英文文章库：列表与详情。 */
@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(articleService.list(level, category, keyword));
    }

    @GetMapping("/{articleId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String articleId) {
        return ApiResponse.ok(articleService.detail(articleId));
    }
}
