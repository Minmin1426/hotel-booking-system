# Implementation Plan: 001-authentication-identity

**Branch**: `001-authentication-identity` | **Date**: 2026-06-23 | **Spec**: [spec.md](spec.md)

## Summary
Implement a stateless JWT-based authentication system covering user registration, login with account lockout, logout with token revocation, password recovery, profile management, and admin user management. All passwords are stored using BCrypt. Every authentication event is recorded in an audit log.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Security 6.3.0, Spring Data JPA
- **Security**: JWT (JJWT 0.12.5), BCrypt (strength 12)
- **Database**: PostgreSQL (Neon cloud) via Flyway migrations
- **Testing**: JUnit 5, Mockito

## Non-Functional Requirements

**Performance**:
- JWT validation SHALL complete in < 100ms using HMAC-SHA signature verification
- No database lookups during token validation (except revocation check via indexed lookup)
- Database indexes on email, token fields for fast lookups

**Scalability**:
- Stateless JWT tokens enable horizontal scaling without session replication
- Token revocation store (revoked_tokens table) should be indexed and regularly cleaned
- Scheduled job (TokenCleanupScheduler) removes expired tokens nightly

**Security**:
- CORS restricted to localhost:5173 only (development) and production URLs
- CSRF protection disabled for stateless JWT endpoints (allowed per AGENTS.md)
- All secrets (JWT_SECRET, DB credentials) via environment variables using ${VAR_NAME} syntax
- Secrets SHALL NOT be hardcoded in source files
- Plaintext passwords SHALL NOT appear in logs or API responses
- Account lockout after 5 failures prevents brute force attacks
- Input validation on all DTOs using Jakarta Bean Validation (@NotNull, @Size, @Email, etc.)
- SQL injection prevention via Spring Data JPA parameterized queries

**Reliability**:
- Automatic cleanup of expired revoked tokens via @Scheduled job
- Comprehensive error handling with GlobalExceptionHandler
- Transaction management (@Transactional) ensures data consistency
- Audit logs record all authentication events for forensics

**Maintainability**:
- DTOs for all REST responses (JPA entities never exposed directly)
- Business logic in service layer (AuthServiceImpl, PasswordServiceImpl, UserServiceImpl)
- No business logic in controllers
- Feature packages maintain proper encapsulation

## Constitution Check
- **Feature Packages**: `com.hotelbooking.auth`, `com.hotelbooking.user`, `com.hotelbooking.common.security`
- **Security**: Public endpoints (`/api/v1/auth/**`, `/api/v1/password/**`) are permit-all. All other endpoints require a valid JWT.
- **DTOs**: All responses use DTOs — JPA entities are never returned directly from REST endpoints.
- **No business logic in controllers**: All business rules are enforced inside `AuthServiceImpl`, `PasswordServiceImpl`, and `UserServiceImpl`.

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/auth/`
  - `AuthController.java` — POST /register, /login, /google, /logout
  - `AuthService.java` — Interface
  - `AuthServiceImpl.java` — Registration, login with lockout, logout, Google login
  - `PasswordController.java` — POST /forgot-password, /reset-password
  - `PasswordService.java` — Interface
  - `PasswordServiceImpl.java` — Token generation (5-min expiry), password update
  - `RevokedToken.java` — Entity for blacklisted JWTs
  - `RevokedTokenRepository.java`
  - `PasswordResetToken.java` — Entity with 5-minute expiry
  - `PasswordResetTokenRepository.java`
  - `dto/` — LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, LogoutRequest, LogoutResponse, ForgotPasswordRequest, ResetPasswordRequest, SocialLoginRequest

- `src/main/java/com/hotelbooking/user/`
  - `UserController.java` — GET/PUT /api/v1/users/me/profile
  - `AdminUserController.java` — GET/POST/PUT/DELETE/PATCH /api/v1/admin/users
  - `UserService.java` / `UserServiceImpl.java`
  - `AdminUserService.java`
  - `User.java` — Entity with role, status, failedLoginAttempts, lastLoginAt
  - `UserRepository.java`
  - `LoginAuditLog.java` — Entity recording each auth event
  - `LoginAuditLogRepository.java`
  - `dto/` — UpdateProfileRequest, UserProfileResponse, UserResponse, CreateUserRequest, UpdateUserRequest, UpdateUserStatusRequest

- `src/main/java/com/hotelbooking/common/security/`
  - `JwtAuthenticationFilter.java` — Intercepts requests, validates JWT, populates SecurityContext
  - `JwtService.java` — Token generation (access: 24h, refresh: 7d), claims extraction, validation
  - `TokenBlacklistService.java` — Checks revoked_tokens on each request
  - `TokenCleanupScheduler.java` — Scheduled job to purge expired revoked tokens

### Database Migrations
- `src/main/resources/db/migration/postgresql/` — Flyway scripts for `users`, `revoked_tokens`, `password_reset_tokens`, `login_audit_logs` tables

### Testing
- `src/test/java/com/hotelbooking/auth/AuthServiceImplTest.java`
- `src/test/java/com/hotelbooking/user/AdminUserServiceTest.java`
- `src/test/java/com/hotelbooking/user/AdminUserControllerTest.java`
