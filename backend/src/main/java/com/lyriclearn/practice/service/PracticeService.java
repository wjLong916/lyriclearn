package com.lyriclearn.practice.service;

import com.lyriclearn.common.BizException;
import com.lyriclearn.omniedu.OmniEduAdapter;
import com.lyriclearn.omniedu.OmniEduDto.GradeResult;
import com.lyriclearn.omniedu.OmniEduDto.QuizQuestion;
import com.lyriclearn.session.entity.LearningSession;
import com.lyriclearn.session.service.SessionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 练习编排服务（架构 6.1）：按学情动态组卷、作答回收、批改；
 * 作答提交同步返回批改结果，学情异步更新。中台不可用时回退静态题。
 */
@Service
public class PracticeService {

    private final OmniEduAdapter omniEdu;
    private final SessionService sessionService;

    public PracticeService(OmniEduAdapter omniEdu, SessionService sessionService) {
        this.omniEdu = omniEdu;
        this.sessionService = sessionService;
    }

    public List<QuizQuestion> quiz(String sessionId) {
        LearningSession s = sessionService.get(sessionId);
        if ("CASUAL".equals(s.getMode())) {
            throw new BizException(400, "休闲听歌模式不生成练习任务");
        }
        return omniEdu.generateQuiz(s.getSongId());
    }

    /** 提交作答：批改并把质量分与断言写回会话，供结算页发起积分发放。 */
    public GradeResult submit(String sessionId, Map<String, String> answers) {
        LearningSession s = sessionService.get(sessionId);
        GradeResult result = omniEdu.grade(s.getSongId(), answers);
        sessionService.finish(sessionId, result.qualityScore(),
                result.assertion() == null ? null : result.assertion().toString());
        return result;
    }
}
