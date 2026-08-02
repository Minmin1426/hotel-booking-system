package com.hotelbooking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatsResponse {
    private long totalCustomers;
    private long activeCustomers;
    private long newCustomersThisMonth;
    private long vipCount;
    private Map<String, Long> byTier;
    private Map<String, Long> byAccountType;
    private Map<String, Long> byStatus;
}
