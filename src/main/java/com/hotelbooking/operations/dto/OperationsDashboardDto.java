package com.hotelbooking.operations.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationsDashboardDto {
    private Double occupancyRate;
    private BigDecimal todayRoomRevenue;
    private BigDecimal todayRestaurantRevenue;
    private Integer totalGroupArrivalsToday;
    private List<UpcomingGroupDto> upcomingGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingGroupDto {
        private Long bookingId;
        private String groupName;
        private String leaderName;
        private String contactPhone;
        private Integer roomCount;
        private String checkInDate;
        private String checkOutDate;
        private String status;
        private Boolean isAllocated;
    }
}
