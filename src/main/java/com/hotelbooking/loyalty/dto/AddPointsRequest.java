package com.hotelbooking.loyalty.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddPointsRequest {
    @NotNull(message = "Points is required")
    @Min(value = 1, message = "Points must be at least 1")
    private Integer points;

    @Size(max = 255, message = "Reason must not exceed 255 characters")
    private String reason;
}
