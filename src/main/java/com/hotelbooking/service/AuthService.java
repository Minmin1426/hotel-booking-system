package com.hotelbooking.service;

import com.hotelbooking.dto.*;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse authenticate(LoginRequest request, String ipAddress, String userAgent);
    LoginResponse loginWithGoogle(SocialLoginRequest request, String ipAddress, String userAgent);
    LoginResponse loginWithFacebook(SocialLoginRequest request, String ipAddress, String userAgent);
    void logout(String authHeader, LogoutRequest request, String ipAddress, String userAgent);
}
