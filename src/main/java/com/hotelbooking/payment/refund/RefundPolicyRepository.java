package com.hotelbooking.payment.refund;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    List<RefundPolicy> findByIsActiveTrueOrderByDaysBeforeCheckinDesc();

    /**
     * Find the matching active policy where daysBeforeCheckin <= daysRemaining.
     * Ordered by daysBeforeCheckin DESC, priority ASC — first result is the most specific rule.
     */
    @Query("SELECT p FROM RefundPolicy p WHERE p.isActive = true AND p.daysBeforeCheckin <= :daysRemaining ORDER BY p.daysBeforeCheckin DESC, p.priority ASC")
    List<RefundPolicy> findMatchingPoliciesOrdered(@Param("daysRemaining") int daysRemaining);

    List<RefundPolicy> findAllByOrderByPriorityAsc();
}
