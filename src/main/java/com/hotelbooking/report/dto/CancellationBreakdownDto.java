package com.hotelbooking.report.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationBreakdownDto {
    private String reason;
    private long count;
    private BigDecimal refundAmount;
}
