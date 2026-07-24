package com.hotelbooking.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByOwnerUserUserId(Long ownerUserId);

    Optional<Wallet> findByOwnerUserUserIdAndWalletType(Long ownerUserId, WalletType walletType);

    Optional<Wallet> findByOwnerUserUserIdAndWalletTypeAndGroupIsNull(Long ownerUserId, WalletType walletType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.walletId = :walletId")
    Optional<Wallet> findByIdWithLock(@Param("walletId") Long walletId);

    @Query("SELECT w FROM Wallet w WHERE w.ownerUser.userId = :userId AND w.walletType = :type")
    Optional<Wallet> findPersonalWallet(@Param("userId") Long userId, @Param("type") WalletType type);

    Page<Wallet> findByOwnerUserUserId(Long ownerUserId, Pageable pageable);

    Page<Wallet> findByStatus(WalletStatus status, Pageable pageable);

    Page<Wallet> findByOwnerUserUserIdAndStatus(Long ownerUserId, WalletStatus status, Pageable pageable);
}
