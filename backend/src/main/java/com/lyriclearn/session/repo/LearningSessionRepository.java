package com.lyriclearn.session.repo;

import com.lyriclearn.session.entity.LearningSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    Optional<LearningSession> findBySessionId(String sessionId);

    long countByUserIdAndSongIdAndStatus(String userId, String songId, String status);

    List<LearningSession> findByUserIdAndStatus(String userId, String status);
}
