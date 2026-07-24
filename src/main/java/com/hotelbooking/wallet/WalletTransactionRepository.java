package com.hotelbooking.wallet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {

    Page<WalletTransaction> findByWalletWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    Page<WalletTransaction> findByWalletWalletIdAndTypeOrderByCreatedAtDesc(
            Long walletId, TransactionType type, Pageable pageable);

    List<WalletTransaction> findByRelatedBookingId(Long bookingId);

    List<WalletTransaction> findByWalletWalletIdAndStatus(Long walletId, TransactionStatus status);
}
