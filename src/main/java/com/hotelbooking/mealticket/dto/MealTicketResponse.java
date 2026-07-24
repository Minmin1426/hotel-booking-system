package com.hotelbooking.mealticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealTicketResponse {
    private Long ticketId;
    private Long userId;
    private String userFullName;
    private Long bookingId;
    private String ticketType;
    private String ticketTypeName;
    private String qrCode;
    private String status;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private String consumedByStaffName;
    private Long issuedByUserId;
    private String notes;
}
