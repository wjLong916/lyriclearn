package com.lyriclearn.omniedu;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static com.lyriclearn.omniedu.OmniEduDto.*;

/**
 * omniEdu 智能教育中台适配器（mock，架构 7）。
 * 生产环境在此屏蔽中台协议/鉴权/版本差异，统一重试、超时、配额与降级；
 * 本阶段以确定性模拟实现解析、组卷、批改与断言签发。中台不可用时业务层回退静态标注。
 */
@Component
public class OmniEduAdapter {

    private final Map<String, List<QuizQuestion>> quizBank = new HashMap<>();
    private final Map<String, List<WordGlossary>> vocab = new HashMap<>();

    public OmniEduAdapter() {
        initBank();
        initVocab();
    }

    private void initBank() {
        quizBank.put("twinkle", List.of(
                new QuizQuestion("tw-q1", "FILL_IN_BLANK", "Twinkle, twinkle, little ___, how I wonder what you are.",
                        List.of("star", "moon", "bird"), "star", "闪烁的对象"),
                new QuizQuestion("tw-q2", "WORD_MATCH", "“wonder” 在句中最接近的含义是？",
                        List.of("想知道", "奇迹", "行走"), "想知道", ""),
                new QuizQuestion("tw-q3", "REWRITE", "用 “up above” 仿写一句描述高处的句子。",
                        List.of("（开放作答，系统按完整度判定）"), "up above", "尝试使用 up above"),
                new QuizQuestion("tw-q4", "ORAL_RECALL", "跟读并复述歌词第二句。",
                        List.of("（语音复述，mock 评分）"), "recall", "注意连读")));

        quizBank.put("lemon", List.of(
                new QuizQuestion("lm-q1", "FILL_IN_BLANK", "I'm sitting here in the ___ room.",
                        List.of("boring", "bright", "clean"), "boring", ""),
                new QuizQuestion("lm-q2", "WORD_MATCH", "“waste my time” 的含义是？",
                        List.of("虚度时光", "节约时间", "等待时间"), "虚度时光", ""),
                new QuizQuestion("lm-q3", "REWRITE", "用 “waiting for” 造句。",
                        List.of("（开放作答）"), "waiting for", ""),
                new QuizQuestion("lm-q4", "ORAL_RECALL", "复述 “It's just another rainy Sunday afternoon.”",
                        List.of("（语音复述，mock 评分）"), "recall", "注意 rainy 的发音")));

        quizBank.put("letitbe", List.of(
                new QuizQuestion("lb-q1", "FILL_IN_BLANK", "Speaking words of wisdom, let it ___.",
                        List.of("be", "go", "see"), "be", ""),
                new QuizQuestion("lb-q2", "WORD_MATCH", "“trouble” 的含义是？",
                        List.of("困境", "旅行", "宝藏"), "困境", ""),
                new QuizQuestion("lb-q3", "REWRITE", "用 “times of trouble” 仿写。",
                        List.of("（开放作答）"), "times of trouble", ""),
                new QuizQuestion("lb-q4", "ORAL_RECALL", "复述 “Mother Mary comes to me.”",
                        List.of("（语音复述，mock 评分）"), "recall", "")));

        quizBank.put("yesterday", List.of(
                new QuizQuestion("ys-q1", "FILL_IN_BLANK", "Yesterday, all my troubles seemed so far ___.",
                        List.of("away", "back", "high"), "away", ""),
                new QuizQuestion("ys-q2", "WORD_MATCH", "“suddenly” 的含义是？",
                        List.of("突然地", "缓慢地", "永远地"), "突然地", ""),
                new QuizQuestion("ys-q3", "REWRITE", "用 “used to be” 写一句回忆。",
                        List.of("（开放作答）"), "used to be", ""),
                new QuizQuestion("ys-q4", "ORAL_RECALL", "复述 “Oh, I believe in yesterday.”",
                        List.of("（语音复述，mock 评分）"), "recall", "")));

        // ===== 英文文章题库（自建文章库，key = articleId） =====
        quizBank.put("art-bee", List.of(
                new QuizQuestion("ab-q1", "FILL_IN_BLANK", "The bee looks for sweet juice called ___.",
                        List.of("nectar", "pollen", "seed"), "nectar", "花蜜"),
                new QuizQuestion("ab-q2", "WORD_MATCH", "“pollen” 的含义是？",
                        List.of("花粉", "花蜜", "种子"), "花粉", ""),
                new QuizQuestion("ab-q3", "REWRITE", "用 “look for” 写一句你寻找某物的话。",
                        List.of("（开放作答）"), "look for", "尝试使用 look for"),
                new QuizQuestion("ab-q4", "WORD_MATCH", "这篇短文主要讲什么？",
                        List.of("蜜蜂无意中帮助植物传粉", "蜜蜂如何酿蜜", "花朵如何结果"), "蜜蜂无意中帮助植物传粉", "主旨")));

        quizBank.put("art-city", List.of(
                new QuizQuestion("ac-q1", "FILL_IN_BLANK", "People choose to walk ___ driving in the city.",
                        List.of("instead", "away", "along"), "instead", ""),
                new QuizQuestion("ac-q2", "WORD_MATCH", "“sidewalk” 的含义是？",
                        List.of("人行道", "斑马线", "自行车道"), "人行道", ""),
                new QuizQuestion("ac-q3", "REWRITE", "用 “be good for” 写一句关于运动的话。",
                        List.of("（开放作答）"), "be good for", ""),
                new QuizQuestion("ac-q4", "WORD_MATCH", "哪一项不是步行的好处？",
                        List.of("需要花钱", "锻炼身体", "不污染空气"), "需要花钱", "细节排除")));

        quizBank.put("art-getty", List.of(
                new QuizQuestion("ag-q1", "FILL_IN_BLANK", "A new nation, conceived in ___, and dedicated to equality.",
                        List.of("liberty", "peace", "war"), "liberty", ""),
                new QuizQuestion("ag-q2", "WORD_MATCH", "“dedicate” 的含义是？",
                        List.of("奉献；致力于", "毁灭", "逃离"), "奉献；致力于", ""),
                new QuizQuestion("ag-q3", "REWRITE", "用 “dedicated to” 写一句你致力于某事的话。",
                        List.of("（开放作答）"), "dedicated to", ""),
                new QuizQuestion("ag-q4", "WORD_MATCH", "这篇演说的核心精神是？",
                        List.of("为自由与平等奉献", "庆祝军事胜利", "怀念战争场面"), "为自由与平等奉献", "主旨")));

        quizBank.put("art-dream", List.of(
                new QuizQuestion("ad-q1", "FILL_IN_BLANK", "Every great ___ begins with a single dream.",
                        List.of("journey", "road", "story"), "journey", ""),
                new QuizQuestion("ad-q2", "WORD_MATCH", "“failure” 的含义是？",
                        List.of("失败", "成功", "恐惧"), "失败", ""),
                new QuizQuestion("ad-q3", "REWRITE", "用 “keep going” 写一句鼓励自己的话。",
                        List.of("（开放作答）"), "keep going", ""),
                new QuizQuestion("ad-q4", "WORD_MATCH", "作者如何看待失败？",
                        List.of("失败是老师，应继续前行", "应当彻底避免", "令人羞耻"), "失败是老师，应继续前行", "态度")));

        // 摘要+外链示范：仅基于平台导读与核心生词命题，不依赖原文
        quizBank.put("art-ted", List.of(
                new QuizQuestion("at-q1", "FILL_IN_BLANK", "A good speech tries to ___ the audience.",
                        List.of("inspire", "stop", "ignore"), "inspire", "激励"),
                new QuizQuestion("at-q2", "WORD_MATCH", "“audience” 的含义是？",
                        List.of("听众；观众", "作者", "评委"), "听众；观众", ""),
                new QuizQuestion("at-q3", "REWRITE", "用 “inspire” 写一句你被某个想法激励的话。",
                        List.of("（开放作答）"), "inspire", "尝试使用 inspire"),
                new QuizQuestion("at-q4", "WORD_MATCH", "本站对这篇第三方演讲的处理方式是？",
                        List.of("仅提供导读摘要，原文外链跳转", "全文收录并允许下载", "翻译后完整转载"), "仅提供导读摘要，原文外链跳转", "版权")));

        // ===== 公有领域诗歌题库（L0–L4，含跟读复述 ORAL_RECALL） =====
        quizBank.put("art-poem-diddle", List.of(
                new QuizQuestion("pd-q1", "FILL_IN_BLANK", "The cow jumped over the ___.",
                        List.of("moon", "cow", "spoon"), "moon", ""),
                new QuizQuestion("pd-q2", "WORD_MATCH", "“fiddle” 的含义是？",
                        List.of("小提琴", "月亮", "勺子"), "小提琴", ""),
                new QuizQuestion("pd-q3", "REWRITE", "用 “ran away” 写一句某物跑开的话。",
                        List.of("（开放作答）"), "ran away", "尝试使用 ran away"),
                new QuizQuestion("pd-q4", "ORAL_RECALL", "跟读并复述 “The cow jumped over the moon.”",
                        List.of("（跟读复述，标记即得分）"), "recall", "注意 jumped 的发音")));

        quizBank.put("art-poem-sheep", List.of(
                new QuizQuestion("ps-q1", "FILL_IN_BLANK", "Little Bo-Peep has lost her ___.",
                        List.of("sheep", "home", "tail"), "sheep", ""),
                new QuizQuestion("ps-q2", "WORD_MATCH", "“tail” 的含义是？",
                        List.of("尾巴", "绵羊", "丢失"), "尾巴", ""),
                new QuizQuestion("ps-q3", "REWRITE", "用 “come home” 写一句关于回家的话。",
                        List.of("（开放作答）"), "come home", ""),
                new QuizQuestion("ps-q4", "ORAL_RECALL", "跟读并复述 “Leave them alone, and they'll come home,”",
                        List.of("（跟读复述，标记即得分）"), "recall", "注意 come home 的连读")));

        quizBank.put("art-poem-lamb", List.of(
                new QuizQuestion("pl-q1", "FILL_IN_BLANK", "Little Lamb, who made ___?",
                        List.of("thee", "me", "the"), "thee", "thee = 你（古英语）"),
                new QuizQuestion("pl-q2", "WORD_MATCH", "“stream” 的含义是？",
                        List.of("小溪；溪流", "羔羊", "草地"), "小溪；溪流", ""),
                new QuizQuestion("pl-q3", "REWRITE", "用 “who made” 仿写一句温柔的问句。",
                        List.of("（开放作答）"), "who made", ""),
                new QuizQuestion("pl-q4", "ORAL_RECALL", "跟读并复述 “Little Lamb, who made thee?”",
                        List.of("（跟读复述，标记即得分）"), "recall", "注意 thee 的发音")));

        quizBank.put("art-poem-tyger", List.of(
                new QuizQuestion("pt-q1", "FILL_IN_BLANK", "Tyger, Tyger, ___ bright,",
                        List.of("burning", "burn", "burnt"), "burning", ""),
                new QuizQuestion("pt-q2", "WORD_MATCH", "“symmetry” 的含义是？",
                        List.of("对称；匀称", "燃烧", "森林"), "对称；匀称", ""),
                new QuizQuestion("pt-q3", "REWRITE", "用 “burning bright” 仿写一句描写光亮的话。",
                        List.of("（开放作答）"), "burning bright", ""),
                new QuizQuestion("pt-q4", "ORAL_RECALL", "跟读并复述 “Tyger, Tyger, burning bright,”",
                        List.of("（跟读复述，标记即得分）"), "recall", "注意 Tyger 的发音")));

        quizBank.put("art-poem-sonnet", List.of(
                new QuizQuestion("pn-q1", "FILL_IN_BLANK", "Shall I ___ thee to a summer's day?",
                        List.of("compare", "lease", "temperate"), "compare", ""),
                new QuizQuestion("pn-q2", "WORD_MATCH", "“temperate” 的含义是？",
                        List.of("温和的；有节制的", "比较", "租期"), "温和的；有节制的", ""),
                new QuizQuestion("pn-q3", "REWRITE", "用 “compare ... to” 写一句比喻。",
                        List.of("（开放作答）"), "compare to", ""),
                new QuizQuestion("pn-q4", "ORAL_RECALL", "跟读并复述 “Shall I compare thee to a summer's day?”",
                        List.of("（跟读复述，标记即得分）"), "recall", "注意 thee 的发音")));
    }

    private void initVocab() {
        vocab.put("twinkle", List.of(
                new WordGlossary("twinkle", "/ˈtwɪŋkl/", "闪烁；闪耀", false),
                new WordGlossary("wonder", "/ˈwʌndə/", "想知道；感到诧异", false),
                new WordGlossary("diamond", "/ˈdaɪəmənd/", "钻石", false)));
        vocab.put("lemon", List.of(
                new WordGlossary("boring", "/ˈbɔːrɪŋ/", "无聊的", false),
                new WordGlossary("waste", "/weɪst/", "浪费；虚度", false),
                new WordGlossary("hang around", "/hæŋ əˈraʊnd/", "闲逛；徘徊", false)));
        vocab.put("letitbe", List.of(
                new WordGlossary("trouble", "/ˈtrʌbl/", "困境；麻烦", false),
                new WordGlossary("wisdom", "/ˈwɪzdəm/", "智慧", false),
                new WordGlossary("darkness", "/ˈdɑːknəs/", "黑暗", false)));
        vocab.put("yesterday", List.of(
                new WordGlossary("trouble", "/ˈtrʌbl/", "烦恼；麻烦", false),
                new WordGlossary("suddenly", "/ˈsʌdnli/", "突然地", false),
                new WordGlossary("shadow", "/ˈʃædoʊ/", "阴影", false)));
    }

    /** 歌词解析包（mock：生词、音标、释义、练习素材）。 */
    public ParsePackage parse(String songId, String cefr, String snapshotVersion) {
        return new ParsePackage(songId, cefr,
                vocab.getOrDefault(songId, List.of()),
                quizBank.getOrDefault(songId, List.of()),
                snapshotVersion == null ? "v1" : snapshotVersion);
    }

    /** 练习组卷（按学情实时生成，mock 返回固定题库）。 */
    public List<QuizQuestion> generateQuiz(String songId) {
        return quizBank.getOrDefault(songId, List.of());
    }

    /**
     * 作答批改：比对选择类题目，开放/语音题按完成度计；
     * 产出质量分并签发学习断言。
     */
    public GradeResult grade(String songId, Map<String, String> answers) {
        List<QuizQuestion> bank = quizBank.getOrDefault(songId, List.of());
        int total = bank.size();
        int correct = 0;
        for (QuizQuestion q : bank) {
            String userAns = answers == null ? null : answers.get(q.id());
            boolean accepted;
            if ("REWRITE".equals(q.type())) {
                // 仿写：输入非空即视为完成，内容由中台/教师复核
                accepted = userAns != null && !userAns.trim().isEmpty();
            } else if ("ORAL_RECALL".equals(q.type())) {
                // 跟读复述：用户标记完成即视为完成（语音评分由中台提供）
                accepted = userAns != null && !userAns.isEmpty();
            } else {
                accepted = userAns != null && userAns.equals(q.answer());
            }
            if (accepted) correct++;
        }
        double accuracy = total == 0 ? 0 : (double) correct / total;
        double qualityScore = Math.round(accuracy * 100) / 100.0;
        List<String> mastered = vocab.getOrDefault(songId, List.of()).stream()
                .map(WordGlossary::word).distinct().toList();
        Assertion assertion = issueAssertion(songId, "v1", qualityScore, mastered);
        String feedback = accuracy >= 0.9 ? "优秀，完成度很高"
                : accuracy >= 0.7 ? "良好，注意薄弱题"
                : accuracy >= 0.4 ? "合格，建议复习生词" : "未达发放积分的质量阈值";
        return new GradeResult(correct, total, accuracy, qualityScore, feedback, assertion);
    }

    /** 签发带签名的学习断言（mock HMAC 风格 SHA-256，供发放前校验）。 */
    public Assertion issueAssertion(String songId, String snapshotVersion, double qualityScore, List<String> mastered) {
        long now = System.currentTimeMillis();
        String payload = songId + "|" + snapshotVersion + "|" + qualityScore + "|" + now;
        return new Assertion(
                "as-" + now,
                songId,
                snapshotVersion,
                qualityScore,
                mastered,
                now,
                sign(payload));
    }

    /** 校验断言签名（账本发放前调用）。 */
    public boolean verifyAssertion(Assertion a) {
        if (a == null) return false;
        String payload = a.songId() + "|" + a.snapshotVersion() + "|" + a.qualityScore() + "|" + a.signedAt();
        return Objects.equals(sign(payload), a.signature())
                && System.currentTimeMillis() - a.signedAt() < 30 * 60 * 1000;
    }

    private String sign(String payload) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(("ll-secret|" + payload).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.substring(0, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
