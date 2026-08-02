package com.hotelbooking.voucher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherStatsResponse {
    private Long voucherId;
    private String code;
    private String name;
    private Long totalClaims;
    private Long totalRedemptions;
    private Integer remainingUsage;
    private BigDecimal totalDiscountGiven;
}
