package com.hotelbooking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDetail360Response {
    // Profile
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String identificationNumber;
    private String role;
    private String status;
    private String accountType;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    // Loyalty
    private String currentTier;
    private LocalDateTime tierEvaluatedAt;
    private BigDecimal lifetimeSpend;
    private Long totalBookings;
    private Long loyaltyPoints;

    // Wallet
    private BigDecimal walletBalance;
    private BigDecimal groupWalletBalance;

    // VIP
    private Boolean isVip;
    private LocalDateTime vipMarkedAt;
    private Long vipMarkedBy;

    // Counts
    private Long activeMealTickets;
    private Long claimedVouchers;
    private Long pinnedNotes;

    // Recent bookings
    private List<BookingSummaryDto> recentBookings;

    // Recent activity
    private List<CustomerActivityEventResponse> recentActivity;

    // Event type breakdown
    private Map<String, Long> eventTypeBreakdown;
}
