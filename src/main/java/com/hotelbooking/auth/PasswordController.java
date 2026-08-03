package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.ForgotPasswordRequest;
import com.hotelbooking.auth.dto.ResetPasswordRequest;
import com.hotelbooking.common.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final AuthService authService;

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        log.info("Received forgot-password request for email: {}", request.getEmail());
        authService.forgotPassword(request.getEmail(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success("If an account exists for that email, an OTP has been sent.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request,
            HttpServletRequest httpRequest) {
        String ipAddress = getClientIp(httpRequest);
        log.info("Received reset-password request for email: {}", request.getEmail());
        authService.resetPasswordWithOtp(request.getEmail(), request.getOtp(), request.getNewPassword(), ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully.", null));
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
