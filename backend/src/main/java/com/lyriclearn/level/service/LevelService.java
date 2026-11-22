package com.lyriclearn.level.service;

import com.lyriclearn.level.entity.UserLevelEntity;
import com.lyriclearn.level.repo.UserLevelRepository;
import com.lyriclearn.learningpath.model.LearningPath;
import com.lyriclearn.learningpath.service.LearningPathService;
import com.lyriclearn.session.entity.LearningSession;
import com.lyriclearn.session.repo.LearningSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/** 级别服务：入学测试定级、按成绩自动评定、按级别推荐路径。 */
@Service
public class LevelService {

    private static final List<String> ORDER = List.of("A1", "A2", "B1", "B2", "C1");

    /** 入学测试题库：A1–C1 每级 2 道单选，逐级全对晋级定级（mock）。 */
    private final List<Map<String, Object>> placement = List.of(
            Map.of("id", "pl-q1", "cefr", "A1", "stem", "What ___ your name?",
                    "options", List.of("is", "are", "am"), "answer", "is"),
            Map.of("id", "pl-q2", "cefr", "A1", "stem", "How ___ apples do you have?",
                    "options", List.of("many", "much", "old"), "answer", "many"),
            Map.of("id", "pl-q3", "cefr", "A2", "stem", "I ___ to school every day.",
                    "options", List.of("go", "goes", "went"), "answer", "go"),
            Map.of("id", "pl-q4", "cefr", "A2", "stem", "She is interested ___ music.",
                    "options", List.of("in", "on", "at"), "answer", "in"),
            Map.of("id", "pl-q5", "cefr", "B1", "stem", "If I ___ rich, I would travel the world.",
                    "options", List.of("were", "am", "was"), "answer", "were"),
            Map.of("id", "pl-q7", "cefr", "B1", "stem", "I wish I ___ how to play the guitar.",
                    "options", List.of("knew", "know", "known"), "answer", "knew"),
            Map.of("id", "pl-q6", "cefr", "B2", "stem", "The report ___ that sales fell significantly.",
                    "options", List.of("suggests", "suggest", "suggesting"), "answer", "suggests"),
            Map.of("id", "pl-q8", "cefr", "B2", "stem", "She is looking forward ___ the new job.",
                    "options", List.of("to starting", "to start", "for starting"), "answer", "to starting"),
            Map.of("id", "pl-q9", "cefr", "C1", "stem", "The proposal was ___ by a unanimous vote.",
                    "options", List.of("adopted", "adapted", "adjusted"), "answer", "adopted"),
            Map.of("id", "pl-q10", "cefr", "C1", "stem", "No sooner ___ the room than the alarm rang.",
                    "options", List.of("had he entered", "he entered", "did he enter"), "answer", "had he entered"));

    private final UserLevelRepository userLevelRepository;
    private final LearningSessionRepository sessionRepository;
    private final LearningPathService learningPathService;

    public LevelService(UserLevelRepository userLevelRepository,
                         LearningSessionRepository sessionRepository,
                         LearningPathService learningPathService) {
        this.userLevelRepository = userLevelRepository;
        this.sessionRepository = sessionRepository;
        this.learningPathService = learningPathService;
    }

    public List<Map<String, Object>> quiz() {
        return placement;
    }

    /**
     * 入学测试判分：A1→C1 逐级考核，某级 2 题全部答对即晋级到该级，
     * 一旦该级未全对则停止晋级，定级到连续全对到达的最高级别；写入级别。
     */
    public Map<String, Object> place(String userId, Map<String, String> answers) {
        int correct = 0;
        String cefr = "A1";
        for (String level : ORDER) {
            List<Map<String, Object>> group = placement.stream()
                    .filter(q -> level.equals(q.get("cefr")))
                    .toList();
            boolean allCorrect = !group.isEmpty();
            for (Map<String, Object> q : group) {
                boolean ok = q.get("answer").equals(answers.get(q.get("id")));
                if (ok) correct++;
                allCorrect = allCorrect && ok;
            }
            if (allCorrect) {
                cefr = level;
            } else {
                break;
            }
        }
        save(userId, cefr, "PLACEMENT");
        return Map.of("correct", correct, "total", placement.size(), "cefr", cefr, "source", "PLACEMENT");
    }

    /** 按已完成会话平均质量分自动评定最高达标级别并保存。 */
    public Map<String, Object> evaluate(String userId) {
        Map<String, Double> avgByCefr = new LinkedHashMap<>();
        Map<String, Integer> countByCefr = new LinkedHashMap<>();
        for (LearningSession s : sessionRepository.findByUserIdAndStatus(userId, "FINISHED")) {
            if (s.getCefrLevel() == null || s.getQualityScore() == null) continue;
            avgByCefr.merge(s.getCefrLevel(), s.getQualityScore(), Double::sum);
            countByCefr.merge(s.getCefrLevel(), 1, Integer::sum);
        }
        String best = null;
        for (String c : ORDER) {
            Double sum = avgByCefr.get(c);
            Integer n = countByCefr.get(c);
            if (sum != null && n != null && sum / n >= 0.7) best = c;
        }
        if (best == null) best = currentCefr(userId);
        save(userId, best, "EVALUATED");
        return Map.of("cefr", best, "source", "EVALUATED",
                "evaluated", !avgByCefr.isEmpty());
    }

    /** 推荐：与当前级别相同或相邻一级的已发布路径。 */
    public Map<String, Object> recommend(String userId) {
        String cefr = currentCefr(userId);
        int idx = Math.max(0, ORDER.indexOf(cefr));
        List<LearningPath> matched = new ArrayList<>();
        for (LearningPath p : learningPathService.listPublished()) {
            int pi = ORDER.indexOf(p.cefr());
            if (pi >= 0 && Math.abs(pi - idx) <= 1) matched.add(p);
        }
        return Map.of("cefr", cefr, "recommended", matched);
    }

    public Map<String, Object> status(String userId) {
        return Map.of("cefr", currentCefr(userId));
    }

    private String currentCefr(String userId) {
        return userLevelRepository.findByUserId(userId)
                .map(UserLevelEntity::getCefr)
                .orElse("A1");
    }

    private void save(String userId, String cefr, String source) {
        UserLevelEntity e = userLevelRepository.findByUserId(userId).orElseGet(UserLevelEntity::new);
        e.setUserId(userId);
        e.setCefr(cefr);
        e.setSource(source);
        e.setUpdatedAt(LocalDateTime.now());
        userLevelRepository.save(e);
    }
}
