package com.hotelbooking.voucher;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    Optional<UserVoucher> findByUserUserIdAndVoucherVoucherId(Long userId, Long voucherId);

    Optional<UserVoucher> findByUserUserIdAndVoucherCode(Long userId, String voucherCode);

    Page<UserVoucher> findByUserUserIdOrderByClaimedAtDesc(Long userId, Pageable pageable);

    Page<UserVoucher> findByUserUserIdAndIsUsedFalseOrderByClaimedAtDesc(Long userId, Pageable pageable);

    long countByVoucherVoucherId(Long voucherId);

    long countByVoucherVoucherIdAndIsUsedTrue(Long voucherId);

    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.voucher.voucherId = :voucherId")
    long countTotalClaims(@Param("voucherId") Long voucherId);

    @Query("SELECT COUNT(uv) FROM UserVoucher uv WHERE uv.voucher.voucherId = :voucherId AND uv.isUsed = true")
    long countTotalRedemptions(@Param("voucherId") Long voucherId);
}
