package com.lyriclearn.session.service;

import com.lyriclearn.common.BizException;
import com.lyriclearn.session.entity.LearningSession;
import com.lyriclearn.session.repo.LearningSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 学习会话服务（架构 6.1）：以会话为单位记录五阶段状态机与事件；
 * 阶段只允许单向推进，异常态单独标记。休闲模式不生成练习、不产出积分。
 */
@Service
public class SessionService {

    private static final List<String> STAGE_ORDER =
            List.of("PREVIEW", "LISTEN", "FOLLOW", "PRACTICE", "REVIEW");

    private final LearningSessionRepository sessionRepository;

    public SessionService(LearningSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    public LearningSession start(String userId, String platform, String songId, String mode, String cefr) {
        long finishedCount = sessionRepository.countByUserIdAndSongIdAndStatus(userId, songId, "FINISHED");
        LearningSession s = new LearningSession();
        s.setSessionId("S" + System.currentTimeMillis() + (int) (Math.random() * 1000));
        s.setUserId(userId);
        s.setPlatform(platform);
        s.setSongId(songId);
        s.setMode(mode == null ? "FOCUS" : mode);
        s.setStage("PREVIEW");
        s.setStatus("IN_PROGRESS");
        s.setCefrLevel(cefr);
        s.setRepeatCount((int) finishedCount);
        return sessionRepository.save(s);
    }

    public LearningSession advance(String sessionId, String nextStage) {
        LearningSession s = get(sessionId);
        int current = STAGE_ORDER.indexOf(s.getStage());
        int next = STAGE_ORDER.indexOf(nextStage);
        if (next < 0 || next != current + 1) {
            throw new BizException(400, "阶段只能按顺序推进：" + s.getStage() + " → 下一阶段");
        }
        s.setStage(nextStage);
        return sessionRepository.save(s);
    }

    public LearningSession finish(String sessionId, Double qualityScore, String assertionJson) {
        LearningSession s = get(sessionId);
        s.setStatus("FINISHED");
        s.setQualityScore(qualityScore);
        s.setAssertionJson(assertionJson);
        s.setFinishedAt(LocalDateTime.now());
        return sessionRepository.save(s);
    }

    public LearningSession get(String sessionId) {
        return sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new BizException(404, "学习会话不存在：" + sessionId));
    }

    public Optional<LearningSession> find(String sessionId) {
        return sessionRepository.findBySessionId(sessionId);
    }

    public Map<String, Object> toMap(LearningSession s) {
        return Map.of(
                "sessionId", s.getSessionId(),
                "songId", s.getSongId(),
                "platform", s.getPlatform(),
                "mode", s.getMode(),
                "stage", s.getStage(),
                "status", s.getStatus(),
                "cefr", s.getCefrLevel() == null ? "" : s.getCefrLevel(),
                "repeatCount", s.getRepeatCount() == null ? 0 : s.getRepeatCount(),
                "qualityScore", s.getQualityScore() == null ? 0 : s.getQualityScore());
    }
}
