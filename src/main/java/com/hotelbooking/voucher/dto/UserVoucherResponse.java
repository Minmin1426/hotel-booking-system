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
public class UserVoucherResponse {
    private Long id;
    private Long voucherId;
    private String voucherCode;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minBookingValue;
    private LocalDateTime endDate;
    private Boolean isUsed;
    private LocalDateTime claimedAt;
    private LocalDateTime usedAt;
    private Long bookingId;
}
