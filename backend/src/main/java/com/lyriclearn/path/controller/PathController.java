package com.lyriclearn.path.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.path.service.PathService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 学习路径：L0–L4 线性关卡与达标解锁。 */
@RestController
@RequestMapping("/api/path")
public class PathController {

    private final PathService pathService;

    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> path(@RequestParam String userId) {
        return ApiResponse.ok(pathService.path(userId));
    }
}
