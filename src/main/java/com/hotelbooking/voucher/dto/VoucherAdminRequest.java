package com.hotelbooking.voucher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class VoucherAdminRequest {
    private String code;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String discountType; // PERCENTAGE or FIXED_AMOUNT

    @NotNull
    @Positive
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;
    private BigDecimal minBookingValue;
    private Integer maxUsage;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String forAccountType; // ALL, CUSTOMER, CORPORATE_MEMBER
}
