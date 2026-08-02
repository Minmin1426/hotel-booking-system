package com.hotelbooking.wallet.dto;

import com.hotelbooking.wallet.WalletStatus;
import com.hotelbooking.wallet.WalletType;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponse {
    private Long walletId;
    private Long ownerUserId;
    private WalletType walletType;
    private Long groupId;
    private String groupName;
    private BigDecimal balance;
    private String currency;
    private WalletStatus status;
}
