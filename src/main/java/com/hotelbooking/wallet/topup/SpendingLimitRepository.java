package com.hotelbooking.wallet.topup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface SpendingLimitRepository extends JpaRepository<SpendingLimit, Long> {

    Optional<SpendingLimit> findByGroupGroupIdAndMemberUserUserId(Long groupId, Long memberUserId);

    @Query("SELECT sl FROM SpendingLimit sl WHERE sl.group.groupId = :groupId " +
           "AND sl.memberUser.userId = :memberUserId " +
           "AND sl.effectiveFrom <= :date " +
           "AND (sl.effectiveUntil IS NULL OR sl.effectiveUntil >= :date)")
    Optional<SpendingLimit> findEffectiveLimit(
            @Param("groupId") Long groupId,
            @Param("memberUserId") Long memberUserId,
            @Param("date") LocalDate date);

    void deleteByGroupGroupIdAndMemberUserUserId(Long groupId, Long memberUserId);
}
