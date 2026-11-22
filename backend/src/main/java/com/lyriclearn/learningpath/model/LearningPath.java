package com.lyriclearn.learningpath.model;

import java.util.List;

/** 混合学习路径：歌曲 + 文章/诗歌组合，标学分供学员挑选；published 表示是否上架。 */
public record LearningPath(
        String pathId,
        String title,
        String description,
        String cefr,
        String level,
        int priceCredits,
        List<PathItem> items,
        boolean published) {
}
