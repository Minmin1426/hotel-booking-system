package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.LoginResponse;
import com.hotelbooking.auth.dto.LogoutRequest;
import com.hotelbooking.auth.dto.RegisterRequest;
import com.hotelbooking.auth.dto.RegisterResponse;
import com.hotelbooking.auth.dto.SocialLoginRequest;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse authenticate(LoginRequest request, String ipAddress, String userAgent);
    LoginResponse loginWithGoogle(SocialLoginRequest request, String ipAddress, String userAgent);
    void logout(String authHeader, LogoutRequest request, String ipAddress, String userAgent);
    void forgotPassword(String email, String ipAddress);
    void resetPassword(String token, String newPassword, String ipAddress);
    void resetPasswordWithOtp(String email, String otp, String newPassword, String ipAddress);
}
