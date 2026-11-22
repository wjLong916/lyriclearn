package com.lyriclearn.session.controller;

import com.lyriclearn.common.ApiResponse;
import com.lyriclearn.session.entity.LearningSession;
import com.lyriclearn.session.service.SessionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 学习会话：创建、阶段推进、查询。 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> start(@RequestBody Map<String, Object> body) {
        String userId = (String) body.get("userId");
        String platform = (String) body.get("platform");
        String songId = (String) body.get("songId");
        String mode = (String) body.getOrDefault("mode", "FOCUS");
        String cefr = (String) body.getOrDefault("cefr", "A2");
        LearningSession s = sessionService.start(userId, platform, songId, mode, cefr);
        return ApiResponse.ok(sessionService.toMap(s));
    }

    @PostMapping("/{sessionId}/advance")
    public ApiResponse<Map<String, Object>> advance(@PathVariable String sessionId,
                                                    @RequestBody Map<String, Object> body) {
        LearningSession s = sessionService.advance(sessionId, (String) body.get("stage"));
        return ApiResponse.ok(sessionService.toMap(s));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<Map<String, Object>> get(@PathVariable String sessionId) {
        return ApiResponse.ok(sessionService.toMap(sessionService.get(sessionId)));
    }
}
