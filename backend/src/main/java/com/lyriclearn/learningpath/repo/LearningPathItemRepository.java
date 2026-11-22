package com.lyriclearn.learningpath.repo;

import com.lyriclearn.learningpath.entity.LearningPathItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningPathItemRepository extends JpaRepository<LearningPathItemEntity, Long> {
    List<LearningPathItemEntity> findByPathIdOrderBySeqAsc(String pathId);

    void deleteByPathId(String pathId);
}
