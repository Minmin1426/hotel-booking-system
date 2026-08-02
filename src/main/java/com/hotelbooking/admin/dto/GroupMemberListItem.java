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
public class GroupMemberListItem {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String status;
    private String currentTier;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
