package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.LoginResponse;
import com.hotelbooking.auth.dto.RegisterRequest;
import com.hotelbooking.auth.dto.RegisterResponse;
import com.hotelbooking.common.exception.EmailAlreadyExistsException;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.common.security.TokenBlacklistService;
import com.hotelbooking.user.LoginAuditLog;
import com.hotelbooking.user.LoginAuditLogRepository;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private LoginAuditLogRepository auditLogRepository;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Quan");
        request.setEmail("quan@gmail.com");
        request.setPassword("12345678");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-password");

        User savedUser = User.builder()
                .userId(1L)
                .email("quan@gmail.com")
                .fullName("Quan")
                .role("CUSTOMER")
                .status("ACTIVE")
                .passwordHash("encoded-password")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("quan@gmail.com", response.getEmail());
        assertEquals("Quan", response.getFullName());
        assertEquals("User registered successfully", response.getMessage());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_email_already_exists() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("quan@gmail.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act + Assert
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("quan@gmail.com");
        request.setPassword("password");

        User user = User.builder()
                .userId(1L)
                .email("quan@gmail.com")
                .passwordHash("encoded-password")
                .role("CUSTOMER")
                .status("ACTIVE")
                .failedLoginAttempts(0)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getEmail(), user.getUserId(), user.getRole())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refresh-token");

        // Act
        LoginResponse response = authService.authenticate(request, "127.0.0.1", "Mozilla/5.0");

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("quan@gmail.com", response.getEmail());
        assertEquals("CUSTOMER", response.getRole());
        assertEquals(0, user.getFailedLoginAttempts());
        assertNotNull(user.getLastLoginAt());

        verify(userRepository).save(user);
        verify(auditLogRepository).save(any(LoginAuditLog.class));
    }

    @Test
    void authenticate_invalid_password_increments_attempts() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("quan@gmail.com");
        request.setPassword("wrong-password");

        User user = User.builder()
                .userId(1L)
                .email("quan@gmail.com")
                .passwordHash("encoded-password")
                .role("CUSTOMER")
                .status("ACTIVE")
                .failedLoginAttempts(2)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.authenticate(request, "127.0.0.1", "Mozilla/5.0"));
        assertEquals(3, user.getFailedLoginAttempts());
        assertEquals("ACTIVE", user.getStatus());

        verify(userRepository).save(user);
        verify(auditLogRepository).save(any(LoginAuditLog.class));
    }

    @Test
    void authenticate_fifth_failed_attempt_locks_account() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("quan@gmail.com");
        request.setPassword("wrong-password");

        User user = User.builder()
                .userId(1L)
                .email("quan@gmail.com")
                .passwordHash("encoded-password")
                .role("CUSTOMER")
                .status("ACTIVE")
                .failedLoginAttempts(4)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPasswordHash())).thenReturn(false);

        // Act & Assert
        assertThrows(LockedException.class, () -> authService.authenticate(request, "127.0.0.1", "Mozilla/5.0"));
        assertEquals(5, user.getFailedLoginAttempts());
        assertEquals("LOCKED", user.getStatus());

        verify(userRepository).save(user);
        verify(auditLogRepository, times(2)).save(any(LoginAuditLog.class));
    }

    @Test
    void authenticate_locked_account_throws_locked_exception() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("quan@gmail.com");
        request.setPassword("any-password");

        User user = User.builder()
                .userId(1L)
                .email("quan@gmail.com")
                .passwordHash("encoded-password")
                .role("CUSTOMER")
                .status("LOCKED")
                .failedLoginAttempts(5)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(LockedException.class, () -> authService.authenticate(request, "127.0.0.1", "Mozilla/5.0"));

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(auditLogRepository).save(any(LoginAuditLog.class));
    }
}
