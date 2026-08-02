package com.hotelbooking.booking.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockBookingResponseDto {
    private Long blockBookingId;
    private String fileName;
    private Integer totalGuests;
    private BigDecimal totalAmount;
    private String status;
    private String rejectionReason;
    private Long requesterId;
    private String requesterEmail;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private List<BlockBookingRowDto> rows;
}
