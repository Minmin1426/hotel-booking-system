package com.hotelbooking.wallet.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayBookingResponse {
    private Long transactionId;
    private Long walletId;
    private BigDecimal amountDeducted;
    private BigDecimal remainingBalance;
    private String bookingStatus;
}
