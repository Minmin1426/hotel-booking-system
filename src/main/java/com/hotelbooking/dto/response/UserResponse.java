package com.hotelbooking.dto.response;

import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String email,
    String fullName,
    String role,
    String status,
    LocalDateTime createdAt
) {}
