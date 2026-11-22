package com.lyriclearn.annotation.controller;

import com.lyriclearn.annotation.service.AnnotationService;
import com.lyriclearn.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** 歌曲详情与标注对齐：GET /api/songs/{platform}/{songId}，歌词仅内存对齐下发。 */
@RestController
@RequestMapping("/api/songs")
public class SongDetailController {

    private final AnnotationService annotationService;

    public SongDetailController(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @GetMapping("/{platform}/{songId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String platform, @PathVariable String songId) {
        return ApiResponse.ok(annotationService.detail(platform, songId));
    }
}
