package com.hotelbooking.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUserStatusRequest(
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(ACTIVE|LOCKED)$", message = "Status must be either ACTIVE or LOCKED")
    String status
) {}
