package com.hotelbooking.voucher;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")})
    Optional<Voucher> findByCode(String code);

    // AC-030: Available vouchers for a given account type
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND v.forAccountType IN ('ALL', :accountType) " +
           "AND (v.startDate IS NULL OR v.startDate <= :now) " +
           "AND (v.endDate IS NULL OR v.endDate >= :now) " +
           "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage) " +
           "ORDER BY v.createdAt DESC")
    Page<Voucher> findAvailableVouchers(
            @Param("accountType") String accountType,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    // Voucher shop: available vouchers that require points to claim
    @Query("SELECT v FROM Voucher v WHERE v.isActive = true " +
           "AND v.pointsCost IS NOT NULL " +
           "AND v.pointsCost > 0 " +
           "AND (v.startDate IS NULL OR v.startDate <= :now) " +
           "AND (v.endDate IS NULL OR v.endDate >= :now) " +
           "AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage) " +
           "AND v.forAccountType IN ('ALL', :accountType) " +
           "ORDER BY v.pointsCost ASC")
    Page<Voucher> findShopVouchers(
            @Param("accountType") String accountType,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}
