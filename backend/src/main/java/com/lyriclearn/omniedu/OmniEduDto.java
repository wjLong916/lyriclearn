package com.lyriclearn.omniedu;

import java.util.List;

/** omniEdu 中台能力契约对象（架构 7.1：解析包 / 组卷 / 批改 / 断言）。 */
public final class OmniEduDto {

    private OmniEduDto() {}

    /** 歌词解析包：生词、音标、语境释义、难度与练习素材。 */
    public record ParsePackage(
            String songId,
            String cefr,
            List<WordGlossary> vocab,
            List<QuizQuestion> practice,
            String version) {}

    public record WordGlossary(String word, String phonetic, String meaning, boolean known) {}

    /** 练习题：四类题型 FILL_IN_BLANK / WORD_MATCH / REWRITE / ORAL_RECALL。 */
    public record QuizQuestion(
            String id,
            String type,
            String stem,
            List<String> options,
            String answer,
            String hint) {}

    /** 作答批改结果。 */
    public record GradeResult(
            int correctCount,
            int total,
            double accuracy,
            double qualityScore,
            String feedback,
            Assertion assertion) {}

    /** 中台学习断言：账本发放前置依据，含防伪造签名。 */
    public record Assertion(
            String assertionId,
            String songId,
            String snapshotVersion,
            double qualityScore,
            List<String> masteredWords,
            long signedAt,
            String signature) {}
}
