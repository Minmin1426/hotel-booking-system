package com.hotelbooking.wallet.topup;

import com.hotelbooking.wallet.topup.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TopUpService {
    TopUpConfigResponse configureAutoTopUp(Long userId, Long walletId, TopUpConfigRequest request);
    TopUpResponse initiateManualTopUp(Long userId, Long walletId, TopUpRequest request);
    void handleStripeWebhook(String payload, String sigHeader);
    Page<TopUpHistoryResponse> getTopUpHistory(Long userId, Long walletId, Pageable pageable);
    void triggerAutoTopUpIfNeeded(Long walletId);
    void updateGlobalLimits(BigDecimal newMaxSingleTopUp, BigDecimal newMaxWalletBalance);
}
