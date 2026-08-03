package com.hotelbooking.auth.dto;

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

    public RegisterResponse(Long userId, String email, String fullName, String message) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.message = message;
    }

    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getMessage() { return message; }
}
