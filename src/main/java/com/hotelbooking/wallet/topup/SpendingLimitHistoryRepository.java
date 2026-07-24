package com.hotelbooking.wallet.topup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpendingLimitHistoryRepository extends JpaRepository<SpendingLimitHistory, Long> {
    Page<SpendingLimitHistory> findBySpendingLimitLimitIdOrderByCreatedAtDesc(Long limitId, Pageable pageable);
}
