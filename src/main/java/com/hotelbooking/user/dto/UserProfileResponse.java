package com.hotelbooking.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String status;
    private String phoneNumber;
    private String identificationNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
