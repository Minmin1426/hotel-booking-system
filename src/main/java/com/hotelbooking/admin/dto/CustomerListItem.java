package com.hotelbooking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerListItem {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String role;
    private String status;
    private String accountType;
    private String currentTier;
    private Boolean isVip;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private BigDecimal walletBalance;
    private Long totalBookings;
}
