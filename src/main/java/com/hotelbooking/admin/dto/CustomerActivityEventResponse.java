package com.hotelbooking.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerActivityEventResponse {
    private Long eventId;
    private String eventType;
    private String eventSummary;
    private String eventMetadata;
    private Long actorUserId;
    private String actorName;
    private LocalDateTime createdAt;
}
