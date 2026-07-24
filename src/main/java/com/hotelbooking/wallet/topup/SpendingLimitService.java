package com.hotelbooking.wallet.topup;

import com.hotelbooking.wallet.topup.dto.SpendingLimitResponse;
import com.hotelbooking.wallet.topup.dto.SpendingLimitRequest;
import com.hotelbooking.wallet.topup.dto.SpendingStatusResponse;

import java.math.BigDecimal;

public interface SpendingLimitService {
    SpendingLimitResponse setMemberLimit(Long adminOrOwnerId, Long groupId, Long memberUserId, SpendingLimitRequest request);
    void checkLimit(Long userId, Long groupId, BigDecimal amount);
    SpendingStatusResponse getSpendingStatus(Long userId, Long groupId);
}
