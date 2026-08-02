package com.hotelbooking.wallet.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletBalanceResponse {
    private Long walletId;
    private BigDecimal balance;
    private String currency;
}
