package com.lyriclearn.evidence.repo;

import com.lyriclearn.evidence.entity.LearningCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningCredentialRepository extends JpaRepository<LearningCredential, Long> {
    Optional<LearningCredential> findByClaimHash(String claimHash);

    Optional<LearningCredential> findByCredentialId(String credentialId);
}
