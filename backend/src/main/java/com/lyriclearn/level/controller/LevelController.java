package com.lyriclearn.level.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.level.service.LevelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 入学测试、自动定级与按级别推荐。 */
@RestController
@RequestMapping("/api")
public class LevelController {

    private final LevelService levelService;

    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }

    @GetMapping("/placement/quiz")
    public ApiResponse<List<Map<String, Object>>> quiz() {
        return ApiResponse.ok(levelService.quiz());
    }

    @PostMapping("/placement/submit")
    public ApiResponse<Map<String, Object>> place(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.getOrDefault("answers", Map.of());
        return ApiResponse.ok(levelService.place((String) body.get("userId"), answers));
    }

    @GetMapping("/level/evaluate")
    public ApiResponse<Map<String, Object>> evaluate(@RequestParam String userId) {
        return ApiResponse.ok(levelService.evaluate(userId));
    }

    @GetMapping("/level/status")
    public ApiResponse<Map<String, Object>> status(@RequestParam String userId) {
        return ApiResponse.ok(levelService.status(userId));
    }

    @GetMapping("/paths/recommend")
    public ApiResponse<Map<String, Object>> recommend(@RequestParam String userId) {
        return ApiResponse.ok(levelService.recommend(userId));
    }
}
