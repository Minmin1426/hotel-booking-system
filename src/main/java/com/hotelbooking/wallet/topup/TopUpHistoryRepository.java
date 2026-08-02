package com.hotelbooking.wallet.topup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TopUpHistoryRepository extends JpaRepository<TopUpHistory, Long> {

    Page<TopUpHistory> findByWalletWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    @Query("SELECT COUNT(h) FROM TopUpHistory h WHERE h.wallet.walletId = :walletId " +
           "AND h.isAutoTopup = true AND h.status = 'SUCCESS' " +
           "AND h.createdAt >= :dayStart AND h.createdAt < :dayEnd")
    long countSuccessfulAutoTopUpsToday(
            @Param("walletId") Long walletId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);

    Optional<TopUpHistory> findByStripeSessionId(String stripeSessionId);
}
