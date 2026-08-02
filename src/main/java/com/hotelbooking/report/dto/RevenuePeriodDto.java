package com.hotelbooking.report.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RevenuePeriodDto {
    private String periodLabel;
    private BigDecimal revenue;
    private Long bookingCount;
}
