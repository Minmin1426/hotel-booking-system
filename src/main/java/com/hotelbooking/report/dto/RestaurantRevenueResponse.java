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
public class RestaurantRevenueResponse {
    private long buffetTicketsSold;
    private BigDecimal totalRevenue;
    private List<RestaurantItemRevenueDto> items;
}
