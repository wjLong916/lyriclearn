package com.lyriclearn.learningpath.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.learningpath.model.LearningPath;
import com.lyriclearn.learningpath.service.LearningPathService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 学员侧：仅返回已发布的混合学习路径。 */
@RestController
@RequestMapping("/api/learning-paths")
public class LearningPathController {

    private final LearningPathService learningPathService;

    public LearningPathController(LearningPathService learningPathService) {
        this.learningPathService = learningPathService;
    }

    @GetMapping
    public ApiResponse<List<LearningPath>> list() {
        return ApiResponse.ok(learningPathService.listPublished());
    }
}
