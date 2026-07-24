package com.hotelbooking.wallet.topup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopUpConfigRepository extends JpaRepository<TopUpConfig, Long> {
    Optional<TopUpConfig> findByWalletWalletId(Long walletId);
    void deleteByWalletWalletId(Long walletId);
}
