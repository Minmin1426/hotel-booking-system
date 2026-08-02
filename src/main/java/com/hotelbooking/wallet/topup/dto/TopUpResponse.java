package com.hotelbooking.wallet.topup.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopUpResponse {
    private String checkoutUrl;
    private String sessionId;
}
