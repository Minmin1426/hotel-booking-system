package com.hotelbooking.loyalty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TierHistoryRepository extends JpaRepository<TierHistory, Long> {

    Page<TierHistory> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
