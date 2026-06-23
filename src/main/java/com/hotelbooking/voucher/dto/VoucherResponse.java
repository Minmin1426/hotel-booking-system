package com.hotelbooking.voucher.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoucherResponse {
    private Long voucherId;
    private String code;
    private String discountType;
    private BigDecimal discountValue;
    private BigDecimal minBookingValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer currentUsage;
}
