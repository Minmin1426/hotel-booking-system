package com.hotelbooking.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String email,
    String fullName,
    String role,
    String status,
    LocalDateTime createdAt
) {}
