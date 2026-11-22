package com.lyriclearn.ledger.repo;

import com.lyriclearn.ledger.entity.LearningAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningAccountRepository extends JpaRepository<LearningAccount, Long> {
    Optional<LearningAccount> findByUserId(String userId);
}
