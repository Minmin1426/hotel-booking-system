package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.ForgotPasswordRequest;
import com.hotelbooking.auth.dto.ResetPasswordRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        passwordService.forgotPassword(request);
        return ResponseEntity.ok("If the email exists, reset instructions have been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        passwordService.resetPassword(request);
        return ResponseEntity.ok("Password reset successfully");
    }
}
