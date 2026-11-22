package com.lyriclearn.learningpath.repo;

import com.lyriclearn.learningpath.entity.LearningPathEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningPathRepository extends JpaRepository<LearningPathEntity, Long> {
    Optional<LearningPathEntity> findByPathId(String pathId);

    List<LearningPathEntity> findByPublishedOrderBySortOrderAsc(String published);

    List<LearningPathEntity> findAllByOrderBySortOrderAsc();

    void deleteByPathId(String pathId);
}
