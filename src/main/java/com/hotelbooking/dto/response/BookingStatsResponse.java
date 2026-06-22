package com.hotelbooking.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatsResponse {
    private Long totalBookings;
    private Long confirmedBookings;
    private Long cancelledBookings;
    private Long pendingBookings;
    private List<DailyBookingStats> dailyStatistics;
    private List<StatusBreakdownDto> statusBreakdown;
}
