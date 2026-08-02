package com.hotelbooking.wallet.topup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SpendingTrackingRepository extends JpaRepository<SpendingTracking, Long> {

    Optional<SpendingTracking> findByUserUserIdAndGroupGroupIdAndPeriodTypeAndPeriodStart(
            Long userId, Long groupId, PeriodType periodType, LocalDate periodStart);

    @Query("SELECT COALESCE(SUM(st.totalSpent), 0) FROM SpendingTracking st " +
           "WHERE st.user.userId = :userId AND st.group.groupId = :groupId " +
           "AND st.periodType = :periodType AND st.periodStart = :periodStart")
    BigDecimal sumSpentForPeriod(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId,
            @Param("periodType") PeriodType periodType,
            @Param("periodStart") LocalDate periodStart);
}
