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
public class RestaurantItemRevenueDto {
    private String itemName;
    private long quantity;
    private BigDecimal revenue;
}
