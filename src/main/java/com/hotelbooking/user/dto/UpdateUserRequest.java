package com.hotelbooking.user.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String email;

    private String fullName;

    private String password; // Optional: if provided, update the user password

    private String role; // CUSTOMER, STAFF, ADMIN, DIRECTOR

    private String status; // ACTIVE, LOCKED, INACTIVE

    private String phoneNumber;

    private String identificationNumber;
}
