package com.hotelbooking.loyalty;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LoyaltyPointLedgerRepository extends JpaRepository<LoyaltyPointLedger, Long> {

    Page<LoyaltyPointLedger> findByUserUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<LoyaltyPointLedger> findFirstByUserUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Sum of all booking payments in the last 12 months for a user.
     * Used to calculate rolling annual spend for tier evaluation.
     */
    @Query(value = """
        SELECT COALESCE(SUM(p.amount), 0)
        FROM payments p
        JOIN bookings b ON p.booking_id = b.booking_id
        WHERE b.user_id = :userId
          AND p.status = 'SUCCESS'
          AND p.created_at >= :fromDate
        """, nativeQuery = true)
    BigDecimal sumSuccessfulPaymentsSince(
            @Param("userId") Long userId,
            @Param("fromDate") LocalDateTime fromDate);
}
