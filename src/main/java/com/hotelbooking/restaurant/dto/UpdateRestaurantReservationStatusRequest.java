package com.hotelbooking.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRestaurantReservationStatusRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(HOLDING|ARRIVED|RELEASED|PAID)$", message = "Status must be HOLDING, ARRIVED, RELEASED, or PAID")
    private String status;
}
