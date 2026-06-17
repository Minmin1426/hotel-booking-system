package com.hotelbooking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBookingStatusRequest(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(CONFIRMED|CANCELLED)$", message = "Status must be either CONFIRMED or CANCELLED")
    String status
) {}
