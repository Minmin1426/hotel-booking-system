package com.hotelbooking.service.impl;

import com.hotelbooking.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendResetPasswordEmail(String email, String token) {
        // Secure practices: Log only necessary info.
        // For development, we print the token to the logs so the developer/user can use it to reset.
        log.info("Sending password reset email to: {}", email);
        log.info("Password Reset Token: {}", token);
        log.info("Reset Password URL: http://localhost:5173/reset-password?token={}", token);
    }
}
