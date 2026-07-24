package com.hotelbooking.user.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateProfileResponse {

    private Long userId;
    private String companyName;
    private String taxCode;
    private String companyAddress;
    private String billingEmail;
    private String ctpStatus;       // NOT_SUBMITTED | PENDING | VERIFIED | REJECTED
    private LocalDateTime ctpVerifiedAt;
    private LocalDateTime ctpSubmittedAt;
    private String message;
}
