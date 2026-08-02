package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.LoginResponse;
import com.hotelbooking.auth.dto.LogoutRequest;
import com.hotelbooking.auth.dto.LogoutResponse;
import com.hotelbooking.auth.dto.RegisterRequest;
import com.hotelbooking.auth.dto.RegisterResponse;
import com.hotelbooking.auth.dto.SocialLoginRequest;
import com.hotelbooking.user.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Received register request for email: {}", request.getEmail());
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("Received login request for email: {} from IP: {}", loginRequest.getEmail(), ipAddress);
        LoginResponse response = authService.authenticate(loginRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(
            @Valid @RequestBody SocialLoginRequest socialLoginRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("Received Google login request from IP: {}", ipAddress);
        LoginResponse response = authService.loginWithGoogle(socialLoginRequest, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody LogoutRequest logoutRequest,
            HttpServletRequest request
    ) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("Received logout request from IP: {}", ipAddress);
        authService.logout(authHeader, logoutRequest, ipAddress, userAgent);
        return ResponseEntity.ok(new LogoutResponse("Logout successful"));
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
