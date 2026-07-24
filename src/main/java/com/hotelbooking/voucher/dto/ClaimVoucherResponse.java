package com.hotelbooking.voucher.dto;

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
public class ClaimVoucherResponse {
    private Long voucherId;
    private String code;
    private String name;
    private String discountType;
    private BigDecimal discountValue;
    private LocalDateTime claimedAt;
    private String message;
}
