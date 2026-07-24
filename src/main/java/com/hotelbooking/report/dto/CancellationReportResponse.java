package com.hotelbooking.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
public class CancellationReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalCancelledBookings;
    private BigDecimal totalRefundAmount;
    private List<CancellationBreakdownDto> breakdown;
}
