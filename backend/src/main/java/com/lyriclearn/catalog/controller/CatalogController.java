package com.lyriclearn.catalog.controller;

import com.lyriclearn.annotation.service.AnnotationService;
import com.lyriclearn.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 曲库检索服务：GET /api/songs 经网关转发第三方检索，返回元信息与是否可学习。 */
@RestController
@RequestMapping("/api/songs")
public class CatalogController {

    private final AnnotationService annotationService;

    public CatalogController(AnnotationService annotationService) {
        this.annotationService = annotationService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String level) {
        return ApiResponse.ok(annotationService.search(keyword, level));
    }
}
