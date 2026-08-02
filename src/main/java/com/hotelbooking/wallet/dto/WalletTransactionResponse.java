package com.hotelbooking.wallet.dto;

import com.hotelbooking.wallet.TransactionStatus;
import com.hotelbooking.wallet.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionResponse {
    private Long transactionId;
    private Long walletId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Long relatedBookingId;
    private TransactionStatus status;
    private String paymentMethod;
    private String description;
    private LocalDateTime createdAt;
}
