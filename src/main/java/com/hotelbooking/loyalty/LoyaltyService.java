package com.hotelbooking.loyalty;

import com.hotelbooking.loyalty.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface LoyaltyService {

    // ── User-facing ──────────────────────────────────────────────────────────────

    TierInfoResponse getMyTier(Long userId);

    Page<TierHistoryResponse> getMyTierHistory(Long userId, Pageable pageable);

    // ── Payment hook (called from PaymentServiceImpl.handlePaymentSuccess) ──────

    void awardPoints(Long userId, Long bookingId, BigDecimal paymentAmount);

    void evaluateTier(Long userId);

    // ── Admin ──────────────────────────────────────────────────────────────────

    List<TierDefinitionResponse> getTierDefinitions(String accountType);

    TierDefinitionResponse updateTierDefinition(Long tierId, TierDefinitionResponse request);

    TierAdjustmentResponse adjustUserTier(Long adminId, Long userId, String tier, String reason);

    Page<PointsLedgerResponse> getUserPointsLedger(Long userId, Pageable pageable);

    PointsLedgerResponse addPointsManually(Long adminId, Long userId, Integer points, String reason);

    void deductPoints(Long userId, Integer points, String reason, Long bookingId, Long voucherId);
}
