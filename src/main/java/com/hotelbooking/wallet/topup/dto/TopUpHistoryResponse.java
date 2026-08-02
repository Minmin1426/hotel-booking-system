package com.hotelbooking.wallet.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopUpHistoryResponse {
    private Long historyId;
    private BigDecimal amount;
    private String paymentMethod;
    private String stripeSessionId;
    private String status;
    private Boolean isAutoTopup;
    private String failureReason;
    private LocalDateTime createdAt;
}
