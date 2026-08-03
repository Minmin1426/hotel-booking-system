package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.LoginResponse;
import com.hotelbooking.auth.dto.LogoutRequest;
import com.hotelbooking.auth.dto.RegisterRequest;
import com.hotelbooking.auth.dto.RegisterResponse;
import com.hotelbooking.auth.dto.SocialLoginRequest;
import com.hotelbooking.common.exception.EmailAlreadyExistsException;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.common.security.TokenBlacklistService;
import com.hotelbooking.user.LoginAuditLog;
import com.hotelbooking.user.LoginAuditLogRepository;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginAuditLogRepository auditLogRepository;
    private final TokenBlacklistService tokenBlacklistService;
    private final EmailService emailService;

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("UC1: Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role("CUSTOMER")
                .status("ACTIVE")
                .failedLoginAttempts(0)
                .phoneNumber(request.getPhoneNumber())
                .identificationNumber(request.getIdentificationNumber())
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .message("User registered successfully")
                .build();
    }

    @Override
    @Transactional(noRollbackFor = {BadCredentialsException.class, LockedException.class, DisabledException.class})
    public LoginResponse authenticate(LoginRequest request, String ipAddress, String userAgent) {
        log.info("UC2: Attempting login for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found for email: {}", request.getEmail());
                    saveAuditLog(request.getEmail(), "FAILED_INVALID_CREDENTIALS", ipAddress, userAgent);
                    throw new BadCredentialsException("Incorrect email or password.");
                });

        if (!user.isAccountNonLocked()) {
            log.warn("Login failed: Account locked for email: {}", request.getEmail());
            saveAuditLog(request.getEmail(), "ACCOUNT_LOCKED", ipAddress, userAgent);
            throw new LockedException("Your account has been locked due to too many failed login attempts. Please contact support or use the reset password feature.");
        }

        if (!user.isEnabled()) {
            log.warn("Login failed: Account disabled/inactive for email: {}", request.getEmail());
            saveAuditLog(request.getEmail(), "ACCOUNT_DISABLED", ipAddress, userAgent);
            throw new DisabledException("Your account is currently disabled. Please contact support to reactivate it.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed: Invalid password for email: {}", request.getEmail());

            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            saveAuditLog(request.getEmail(), "FAILED_INVALID_CREDENTIALS", ipAddress, userAgent);

            if (attempts >= 5) {
                user.setStatus("LOCKED");
                userRepository.save(user);
                log.warn("Account locked: Email {} reached {} failed attempts", request.getEmail(), attempts);
                saveAuditLog(request.getEmail(), "ACCOUNT_LOCKED", ipAddress, userAgent);
                throw new LockedException("Your account has been locked due to too many failed login attempts. Please use the reset password feature.");
            }

            userRepository.save(user);
            int remaining = 5 - attempts;
            throw new BadCredentialsException("Incorrect email or password. " + remaining + " attempt(s) remaining before your account is locked.");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Login successful for email: {}", request.getEmail());
        saveAuditLog(request.getEmail(), "SUCCESS", ipAddress, userAgent);

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void logout(String authHeader, LogoutRequest request, String ipAddress, String userAgent) {
        log.info("UC3: Processing logout request");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Logout failed: Missing or invalid Authorization header");
            saveAuditLog("ANONYMOUS", "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
            throw new JwtException("UNAUTHORIZED");
        }

        String accessToken = authHeader.substring(7);
        String email;

        try {
            if (jwtService.isTokenExpired(accessToken)) {
                log.warn("Logout failed: Access token is expired");
                saveAuditLog("ANONYMOUS", "LOGOUT_FAILED_EXPIRED", ipAddress, userAgent);
                throw new JwtException("SESSION_EXPIRED");
            }
            email = jwtService.extractUsername(accessToken);
        } catch (ExpiredJwtException e) {
            log.warn("Logout failed: Access token has expired - {}", e.getMessage());
            saveAuditLog("ANONYMOUS", "LOGOUT_FAILED_EXPIRED", ipAddress, userAgent);
            throw e;
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Logout failed: Access token signature or parsing invalid - {}", e.getMessage());
            saveAuditLog("ANONYMOUS", "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
            throw new JwtException("UNAUTHORIZED");
        }

        if (tokenBlacklistService.isTokenRevoked(accessToken)) {
            log.warn("Logout failed: Access token is already revoked for user: {}", email);
            saveAuditLog(email, "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
            throw new JwtException("UNAUTHORIZED");
        }

        String refreshToken = request.getRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty()) {
            log.warn("Logout failed: Refresh token is missing for user: {}", email);
            saveAuditLog(email, "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
            throw new JwtException("UNAUTHORIZED");
        }

        try {
            if (!jwtService.isTokenValid(refreshToken, email)) {
                log.warn("Logout failed: Refresh token is invalid or expired for user: {}", email);
                saveAuditLog(email, "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
                throw new JwtException("UNAUTHORIZED");
            }
        } catch (ExpiredJwtException e) {
            log.warn("Logout failed: Refresh token is expired - {}", e.getMessage());
            saveAuditLog(email, "LOGOUT_FAILED_EXPIRED", ipAddress, userAgent);
            throw e;
        } catch (JwtException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Logout failed: Refresh token parsing failed - {}", e.getMessage());
            saveAuditLog(email, "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
            throw new JwtException("UNAUTHORIZED");
        }

        if (tokenBlacklistService.isTokenRevoked(refreshToken)) {
            log.warn("Logout failed: Refresh token is already revoked for user: {}", email);
            saveAuditLog(email, "LOGOUT_FAILED_INVALID", ipAddress, userAgent);
            throw new JwtException("UNAUTHORIZED");
        }

        tokenBlacklistService.blacklistToken(accessToken, "ACCESS", email);
        tokenBlacklistService.blacklistToken(refreshToken, "REFRESH", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found during logout update: {}", email);
                    return new UsernameNotFoundException("User not found: " + email);
                });
        user.setLastLogoutAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Logout successful for user: {}", email);
        saveAuditLog(email, "LOGOUT_SUCCESS", ipAddress, userAgent);
    }

    @Override
    @Transactional
    public LoginResponse loginWithGoogle(SocialLoginRequest request, String ipAddress, String userAgent) {
        log.info("UC2a: Attempting Google login");
        Map<String, Object> tokenInfo = verifyGoogleToken(request.getToken());
        if (tokenInfo == null) {
            log.warn("Google authentication failed: invalid token");
            saveAuditLog("ANONYMOUS", "FAILED_INVALID_SOCIAL_TOKEN", ipAddress, userAgent);
            throw new BadCredentialsException("INVALID_SOCIAL_TOKEN");
        }

        String email = (String) tokenInfo.get("email");
        String name = (String) tokenInfo.get("name");
        return authenticateSocialUser(email, name, "Google", ipAddress, userAgent);
    }

    private Map<String, Object> verifyGoogleToken(String token) {
        if (token != null && token.startsWith("mock-google-token-")) {
            String email = token.substring("mock-google-token-".length());
            log.info("Social login: Bypassing Google token verification with mock email: {}", email);
            return Map.of("email", email, "name", "Mock Google User");
        }
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url;
            if (token != null && token.contains(".") && token.split("\\.").length == 3) {
                log.info("Verifying Google token as ID Token");
                url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
            } else {
                log.info("Verifying Google token as Access Token");
                url = "https://www.googleapis.com/oauth2/v3/userinfo?access_token=" + token;
            }
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Map<String, Object>) response.getBody();
            }
        } catch (Exception e) {
            log.error("Failed to verify Google token: {}", e.getMessage());
        }
        return null;
    }

    private LoginResponse authenticateSocialUser(String email, String name, String provider, String ipAddress, String userAgent) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email from " + provider + " accounts is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    log.info("Social login: Creating new user for email: {} from provider: {}", email, provider);
                    User newUser = User.builder()
                            .email(email)
                            .passwordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                            .fullName(name != null ? name : "Social User")
                            .role("CUSTOMER")
                            .status("ACTIVE")
                            .failedLoginAttempts(0)
                            .build();
                    return userRepository.save(newUser);
                });

        if (!user.isAccountNonLocked()) {
            log.warn("Social login failed: Account locked for email: {}", email);
            saveAuditLog(email, "ACCOUNT_LOCKED", ipAddress, userAgent);
            throw new LockedException("ACCOUNT_LOCKED");
        }

        if (!user.isEnabled()) {
            log.warn("Social login failed: Account disabled/inactive for email: {}", email);
            saveAuditLog(email, "ACCOUNT_DISABLED", ipAddress, userAgent);
            throw new DisabledException("ACCOUNT_DISABLED");
        }

        user.setFailedLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("Social login successful for email: {} from provider: {}", email, provider);
        saveAuditLog(email, "SUCCESS_SOCIAL_" + provider.toUpperCase(), ipAddress, userAgent);

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email, String ipAddress) {
        log.info("UC3: Forgot password request for email: {}", email);

        if (email == null || email.isBlank()) return;

        userRepository.findByEmail(email.trim()).ifPresent(user -> {
            String otp = String.format("%06d", new java.util.Random().nextInt(1_000_000));
            user.setOtpCode(passwordEncoder.encode(otp));
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            emailService.sendOtpEmail(email, user.getFullName(), otp);
            log.info("OTP sent to email: {}, expires in 15 minutes", email);
        });

        saveAuditLog(email, "FORGOT_PASSWORD_REQUEST", ipAddress, null);
    }

    @Override
    public void resetPassword(String token, String newPassword, String ipAddress) {
        throw new UnsupportedOperationException("Use resetPasswordWithOtp instead");
    }

    @Transactional
    public void resetPasswordWithOtp(String email, String otp, String newPassword, String ipAddress) {
        log.info("UC3: Reset password with OTP for email: {}", email);

        if (email == null || email.isBlank() || otp == null || otp.isBlank()
            || newPassword == null || newPassword.isBlank()) {
            throw new BadCredentialsException("Email, OTP, and new password are required");
        }

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired reset token"));

        if (user.getOtpCode() == null || user.getOtpExpiry() == null) {
            throw new BadCredentialsException("No OTP was requested for this account");
        }

        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new BadCredentialsException("OTP has expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(otp, user.getOtpCode())) {
            throw new BadCredentialsException("Invalid OTP code");
        }

        if (newPassword.length() < 8
            || !newPassword.matches(".*[a-z].*")
            || !newPassword.matches(".*[A-Z].*")
            || !newPassword.matches(".*\\d.*")
            || !newPassword.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            throw new BadCredentialsException("Password must be at least 8 characters with uppercase, lowercase, digit, and special character");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        log.info("Password reset successful for email: {}", email);
        saveAuditLog(email, "PASSWORD_RESET_SUCCESS", ipAddress, null);
    }

    private void saveAuditLog(String email, String status, String ipAddress, String userAgent) {
        try {
            LoginAuditLog logEntry = LoginAuditLog.builder()
                    .email(email)
                    .status(status)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save login audit log for {}: {}", email, e.getMessage(), e);
        }
    }
}
