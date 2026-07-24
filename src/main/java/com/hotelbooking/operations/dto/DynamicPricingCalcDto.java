package com.hotelbooking.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class DynamicPricingCalcDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Request {
        private Long hotelId;
        private Integer roomQuantity;
        private BigDecimal originalUnitPrice;
        private String checkInDate;
        private String checkOutDate;
        private Boolean isWeekend;
        private Boolean isPeakSeason;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Integer roomQuantity;
        private BigDecimal originalUnitPrice;
        private BigDecimal baseTotalPrice;
        private BigDecimal discountPercentage;
        private BigDecimal discountAmount;
        private BigDecimal weekendSurchargeAmount;
        private BigDecimal finalTotalPrice;
        private BigDecimal finalEffectiveUnitPrice;
        private String AppliedRuleSummary;
    }
}
