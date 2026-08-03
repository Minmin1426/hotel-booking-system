package com.hotelbooking.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBookingStatusRequest(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(CONFIRMED|CANCELLED|CHECKED_IN|CHECKED_OUT)$", message = "Status must be CONFIRMED, CANCELLED, CHECKED_IN or CHECKED_OUT")
    String status
) {}

