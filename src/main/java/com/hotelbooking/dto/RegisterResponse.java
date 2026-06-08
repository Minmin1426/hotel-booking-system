package com.hotelbooking.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {

    private Long userId;
    private String email;
    private String fullName;
    private String message;
}
