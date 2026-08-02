package com.hotelbooking.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CorporateProfileRequest {

    @NotBlank(message = "Company name is required")
    @Size(max = 255, message = "Company name must not exceed 255 characters")
    private String companyName;

    @NotBlank(message = "Tax code is required")
    @Size(max = 50, message = "Tax code must not exceed 50 characters")
    private String taxCode;

    @NotBlank(message = "Company address is required")
    private String companyAddress;

    @NotBlank(message = "Billing email is required")
    @Email(message = "Invalid billing email format")
    private String billingEmail;
}
