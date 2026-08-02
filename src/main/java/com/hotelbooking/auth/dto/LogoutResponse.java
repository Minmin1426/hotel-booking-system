package com.hotelbooking.auth.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogoutResponse {
    private String message;
}
