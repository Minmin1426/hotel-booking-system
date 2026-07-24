package com.hotelbooking.report.dto;

import java.math.BigDecimal;
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
public class GroupRevenueReportResponse {
    private BigDecimal roomRevenue;
    private BigDecimal restaurantRevenue;
    private BigDecimal surchargeRevenue;
    private BigDecimal cancellationRate;
    private List<RevenuePeriodDto> periodRevenue;
}
