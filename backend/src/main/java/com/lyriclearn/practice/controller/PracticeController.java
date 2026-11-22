package com.lyriclearn.practice.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.omniedu.OmniEduDto.GradeResult;
import com.lyriclearn.omniedu.OmniEduDto.QuizQuestion;
import com.lyriclearn.practice.service.PracticeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** 练习编排：组卷与作答批改。 */
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practiceService;

    public PracticeController(PracticeService practiceService) {
        this.practiceService = practiceService;
    }

    @GetMapping("/{sessionId}/quiz")
    public ApiResponse<List<QuizQuestion>> quiz(@PathVariable String sessionId) {
        return ApiResponse.ok(practiceService.quiz(sessionId));
    }

    @PostMapping("/{sessionId}/submit")
    public ApiResponse<GradeResult> submit(@PathVariable String sessionId,
                                          @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        Map<String, String> answers = (Map<String, String>) body.getOrDefault("answers", Map.of());
        return ApiResponse.ok(practiceService.submit(sessionId, answers));
    }
}
