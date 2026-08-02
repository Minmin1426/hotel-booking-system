package com.hotelbooking.mealticket.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WristbandResponse {
    private Long wristbandId;
    private String wristbandCode;
    private Long bookingId;
    private String bookingCode;
    private String guestName;
    private String roomNumber;
    private String colorCode;
    private String packageName;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime returnedAt;
    private String notes;
}
