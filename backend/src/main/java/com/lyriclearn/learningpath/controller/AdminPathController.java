package com.lyriclearn.learningpath.controller;

import com.lyriclearn.annotation.service.AnnotationService;
import com.lyriclearn.article.service.ArticleService;
import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.learningpath.model.LearningPath;
import com.lyriclearn.learningpath.service.LearningPathService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 管理员：路径编排（CRUD/定价/上下架）与可选资源枚举。仅 ADMIN 调用。 */
@RestController
@RequestMapping("/api/admin/paths")
public class AdminPathController {

    private final LearningPathService pathService;
    private final AnnotationService annotationService;
    private final ArticleService articleService;

    public AdminPathController(LearningPathService pathService,
                               AnnotationService annotationService,
                               ArticleService articleService) {
        this.pathService = pathService;
        this.annotationService = annotationService;
        this.articleService = articleService;
    }

    @GetMapping
    public ApiResponse<List<LearningPath>> all() {
        return ApiResponse.ok(pathService.listAll());
    }

    @PostMapping
    public ApiResponse<LearningPath> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(pathService.create(body));
    }

    @PutMapping("/{pathId}")
    public ApiResponse<LearningPath> update(@PathVariable String pathId, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(pathService.update(pathId, body));
    }

    @DeleteMapping("/{pathId}")
    public ApiResponse<Object> delete(@PathVariable String pathId) {
        pathService.delete(pathId);
        return ApiResponse.ok(Map.of("deleted", pathId));
    }

    /** 可选资源枚举：歌曲 + 文章（新闻/演讲稿/诗歌），供管理员勾选组合。 */
    @GetMapping("/resources")
    public ApiResponse<List<Map<String, Object>>> resources() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map<String, Object> s : annotationService.search(null, null)) {
            out.add(Map.of(
                    "type", "SONG",
                    "ref", s.get("platform") + ":" + s.get("songId"),
                    "title", s.get("title"),
                    "cefr", s.get("cefr")));
        }
        for (Map<String, Object> a : articleService.list(null, null, null)) {
            out.add(Map.of(
                    "type", "ARTICLE",
                    "ref", a.get("articleId"),
                    "title", a.get("title"),
                    "cefr", a.get("cefr")));
        }
        return ApiResponse.ok(out);
    }
}
