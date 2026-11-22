package com.lyriclearn.annotation.repo;

import com.lyriclearn.annotation.entity.SongAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongAnnotationRepository extends JpaRepository<SongAnnotation, Long> {
    Optional<SongAnnotation> findByPlatformAndSongId(String platform, String songId);

    List<SongAnnotation> findByReviewStatusAndLearnableTrue(String reviewStatus);
}
