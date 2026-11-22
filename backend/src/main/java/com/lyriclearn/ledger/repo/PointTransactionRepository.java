package com.lyriclearn.ledger.repo;

import com.lyriclearn.ledger.entity.PointTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {
    Optional<PointTransaction> findByIdempotentKey(String idempotentKey);

    Page<PointTransaction> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
