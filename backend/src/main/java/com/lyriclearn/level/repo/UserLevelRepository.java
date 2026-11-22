package com.lyriclearn.level.repo;

import com.lyriclearn.level.entity.UserLevelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserLevelRepository extends JpaRepository<UserLevelEntity, Long> {
    Optional<UserLevelEntity> findByUserId(String userId);
}
