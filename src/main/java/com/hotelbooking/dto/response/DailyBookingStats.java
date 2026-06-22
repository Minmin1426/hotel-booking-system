package com.hotelbooking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DailyBookingStats {
    private LocalDate date;
    private Long totalCount;
    private Long confirmedCount;
    private Long cancelledCount;
}
