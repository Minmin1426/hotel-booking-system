# Tasks: 001-authentication-identity

**Input**: Design documents from `/specs/001-authentication-identity/`

## Phase 1: Database & Entities (Data Model)
- [x] T001 Create Flyway migration for `users` table with columns: `user_id`, `email`, `password_hash`, `full_name`, `role`, `status`, `failed_login_attempts`, `last_login_at`, `phone_number`, `identification_number`, `created_at`, `updated_at`
- [x] T002 Create Flyway migration for `revoked_tokens` table with columns: `id`, `token` (unique), `revoked_at`, `expires_at`
- [x] T003 Create Flyway migration for `password_reset_tokens` table with columns: `id`, `token`, `email`, `expiry_time`, `created_at`
- [x] T004 Create Flyway migration for `login_audit_logs` table with columns: `id`, `email`, `status`, `ip_address`, `user_agent`, `created_at`
- [x] T005 Implement `User` JPA entity implementing `UserDetails` with `isAccountNonLocked()` checking status field
- [x] T006 Implement `RevokedToken`, `PasswordResetToken`, `LoginAuditLog` entities and repositories

## Phase 2: Security Infrastructure
- [x] T007 Implement `JwtService` — generate access token (24h), refresh token (7d), extract claims (userId, role, email)
- [x] T008 Implement `TokenBlacklistService` — check if a token exists in `revoked_tokens`
- [x] T009 Implement `JwtAuthenticationFilter` — extract JWT from Authorization header, validate, check revocation, set SecurityContext
- [x] T010 Configure `SecurityConfig` — permit-all for `/api/v1/auth/**` and `/api/v1/password/**`, require auth for all other endpoints, enable `@PreAuthorize`
- [x] T011 Implement `TokenCleanupScheduler` — scheduled job to delete expired revoked tokens

## Phase 3: Authentication (UC-01, UC-02, UC-03)
- [x] T012 Implement `AuthServiceImpl.register()` — validate unique email, encode password with BCrypt, save user with role=CUSTOMER and status=ACTIVE
- [x] T013 Implement `AuthServiceImpl.authenticate()` — verify account status (LOCKED/INACTIVE), verify password, increment `failedLoginAttempts` on failure, lock account at 5 attempts, reset counter and record `lastLoginAt` on success, save audit log with IP and User-Agent
- [x] T014 Implement `AuthServiceImpl.logout()` — extract JWT from Authorization header, save to `revoked_tokens`
- [x] T015 Implement `AuthServiceImpl.googleLogin()` — validate Google token, find or create user, generate JWT
- [x] T016 Expose `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/logout`, `POST /api/v1/auth/google` in `AuthController`

## Phase 4: Password Management (UC-32)
- [x] T017 Implement `PasswordServiceImpl.forgotPassword()` — find user by email, generate reset token, set 5-minute expiry, save `PasswordResetToken`
- [x] T018 Implement `PasswordServiceImpl.resetPassword()` — validate token exists and not expired, encode new password with BCrypt, save, invalidate token
- [x] T019 Expose `POST /api/v1/password/forgot-password` and `POST /api/v1/password/reset-password` in `PasswordController`

## Phase 5: User Profile (UC-05)
- [x] T020 Implement `UserServiceImpl.getProfile()` — load user by userId extracted from JWT, return `UserProfileResponse`
- [x] T021 Implement `UserServiceImpl.updateProfile()` — validate new email not taken by another user, update fullName, email, phoneNumber, identificationNumber
- [x] T022 Expose `GET /api/v1/users/me/profile` and `PUT /api/v1/users/me/profile` in `UserController` secured with `isAuthenticated()`

## Phase 6: Admin User Management (UC-23)
- [x] T023 Implement `AdminUserService` — get all users (paginated), create user, update user, delete user, update user status (ACTIVE/LOCKED)
- [x] T024 Expose `GET`, `POST`, `PUT`, `DELETE /api/v1/admin/users` and `PATCH /api/v1/admin/users/{id}/status` in `AdminUserController` secured with `@PreAuthorize("hasRole('ADMIN')")`

## Phase 7: Error Handling
- [x] T025 Implement `GlobalExceptionHandler` @RestControllerAdvice to handle all authentication exceptions
- [x] T026 Map exceptions to correct HTTP status codes (401 for auth failures, 400 for validation, 403 for access denied, 404 for not found)
- [x] T027 Ensure error responses follow standard format: timestamp, status, error, message, path

## Phase 8: Testing & Validation
- [x] T028 Write unit tests in `AuthServiceImplTest` covering: register success, duplicate email, login success, login with wrong password (increment counter), login locked account, login after 5 failures triggers lock
- [x] T029 Write unit tests in `AdminUserServiceTest` covering: get all users, create user, update status to LOCKED
- [x] T030 Write integration/controller tests in `AdminUserControllerTest` covering: unauthorized access returns 403, admin access returns 200
- [x] T031 Verify all 6 success criteria (SC-001 to SC-006) are testable and documented

## Out of Scope (Phase 1)

The following features are explicitly out of scope for this phase:
- Email verification workflows
- Two-factor authentication (2FA)
- Social login beyond Google OAuth
- API key authentication
- Passwordless authentication
- Account recovery via SMS
- Password strength meter UI

These may be addressed in future phases based on business requirements.

