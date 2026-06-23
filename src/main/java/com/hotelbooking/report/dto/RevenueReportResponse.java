package com.hotelbooking.report.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportResponse {
    private BigDecimal totalRevenue;
    private List<RevenuePeriodDto> periodRevenue;
    private List<HotelRevenueDto> revenueByHotel;
}
