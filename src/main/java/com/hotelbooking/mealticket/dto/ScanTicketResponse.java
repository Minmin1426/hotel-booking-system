package com.hotelbooking.mealticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScanTicketResponse {
    private Long ticketId;
    private Long userId;
    private String userFullName;
    private String ticketType;
    private String status;
    private LocalDateTime consumedAt;
}
