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
public class VoucherStoreResponse {
    private Long voucherId;
    private String code;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minBookingValue;
    private Integer maxUsage;
    private Integer currentUsage;
    private Integer remainingUsage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String forAccountType;
    private Integer pointsCost;
}
