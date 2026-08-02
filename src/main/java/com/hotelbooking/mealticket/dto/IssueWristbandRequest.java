package com.hotelbooking.mealticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssueWristbandRequest {

    @NotNull(message = "Booking ID is required")
    private Long bookingId;

    @NotBlank(message = "Wristband code is required")
    private String wristbandCode;

    // RED, BLUE, GOLD, GREEN
    @Builder.Default
    private String colorCode = "BLUE";

    @Builder.Default
    private String packageName = "Breakfast Buffet";

    private String notes;
}
