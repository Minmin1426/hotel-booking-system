package com.hotelbooking.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CtpVerificationSummary {

    private Long userId;
    private String email;
    private String fullName;
    private String companyName;
    private String taxCode;
    private String billingEmail;
    private String ctpStatus;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;
}
