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
public class TopUpConfigResponse {
    private Long configId;
    private Boolean enabled;
    private BigDecimal thresholdAmount;
    private BigDecimal topupAmount;
    private Integer maxDailyAutoTopup;
    private LocalDateTime updatedAt;
}
