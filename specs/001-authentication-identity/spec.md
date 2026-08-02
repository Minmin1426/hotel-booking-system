# Feature Specification: 001-authentication-identity

**Feature Branch**: `001-authentication-identity`
**Created**: 2026-06-23
**Status**: Completed
**Primary Actor(s)**: Guest, Customer, Admin
**Related Use Cases**: UC-01, UC-02, UC-03, UC-04, UC-05, UC-23, UC-32

---

## 1. Context & Goal

The hotel booking system requires a secure, stateless authentication layer to enable users to register, log in, manage profiles, and allow administrators to control account access. This feature provides JWT-based authentication with account lockout protection, token revocation on logout, password recovery, and comprehensive audit logging.

**Goal**: Implement a production-ready authentication system that supports user registration, secure login with lockout, token-based session management, and admin oversight.

---

## 2. Actors & Roles

**Guest**: Unauthenticated user
- Registers new account
- Logs in
- Requests password reset
- Access public endpoints only

**Customer**: Authenticated user (role = CUSTOMER)
- Views and updates own profile
- Accesses booking features
- Logs out to revoke tokens
- Cannot access admin endpoints

**Admin**: Authenticated user (role = ADMIN)
- Views all user accounts
- Creates, updates, locks, unlocks user accounts
- Accesses administrative endpoints
- Cannot access customer features requiring specific user context

**Director**: Authenticated user (role = DIRECTOR)
- Has potential admin-level capabilities
- Reserved for future expansion

**STAFF**: Authenticated user (role = STAFF)
- Designated for hotel staff operations
- Reserved for future expansion

---

## 3. Functional Requirements

THE system SHALL hash passwords using BCrypt with strength 12 or higher.
THE system SHALL NOT store plaintext passwords.

THE system SHALL assign role = CUSTOMER and status = ACTIVE to newly registered users.

WHEN a user attempts login with wrong password, THE system SHALL increment failedLoginAttempts counter.
WHEN failedLoginAttempts reaches 5, THE system SHALL set account status to LOCKED.
WHILE account status is LOCKED or INACTIVE, THE system SHALL reject login attempts with HTTP 401 ACCOUNT_LOCKED.

THE system SHALL generate access tokens with 24-hour (86400 seconds) expiration.
THE system SHALL generate refresh tokens with 7-day (604800000 milliseconds) expiration.
THE system SHALL include userId, email, and role in JWT claims.

WHEN user logs out, THE system SHALL revoke the current access token by storing it in revoked_tokens table.
WHEN an authenticated request arrives, THE system SHALL check if the token exists in revoked_tokens.
WHERE token is revoked, THE system SHALL reject the request with HTTP 401 TOKEN_REVOKED.

WHEN password reset is requested, THE system SHALL generate a reset token with 5-minute expiration.
WHERE reset token is expired, THE system SHALL reject reset password attempts.

THE system SHALL record every login attempt (success or failure) in login_audit_logs with email, status, ipAddress, userAgent, and timestamp.

THE system SHALL validate profile update requests to ensure new email addresses are not already in use by another account.

---

## 4. Non-Functional Requirements

**Performance**: Access token validation SHALL complete in < 100ms using in-memory signature verification.

**Scalability**: THE system SHALL use stateless JWT tokens to enable horizontal scaling without session replication.

**Security**:
- THE system SHALL restrict CORS to known frontend origins only (never allowedOrigins("*")).
- THE system SHALL disable CSRF protection for stateless JWT endpoints.
- THE system SHALL store all secrets (JWT_SECRET) via environment variables using ${VARIABLE_NAME} placeholder syntax.
- THE system SHALL NOT log plaintext passwords or sensitive payment information.
- WHERE account is locked, THE system SHALL not reveal whether the email exists by using generic error messages.

**Reliability**:
- THE system SHALL automatically clean up expired revoked tokens via scheduled job.
- THE system SHALL validate all input DTOs using Jakarta Bean Validation constraints.

**Maintainability**:
- THE system SHALL use DTOs for all REST responses (JPA entities never exposed directly).
- THE system SHALL enforce business logic in service layer (no logic in controllers).

---

## 5. Data Model

The authentication system manages the following entities:

**User**
- user_id (PK, auto-increment)
- email (unique, not null)
- password_hash (not null, BCrypt format)
- full_name (not null)
- role (not null: CUSTOMER, ADMIN, DIRECTOR, STAFF)
- status (not null: ACTIVE, LOCKED, INACTIVE)
- failed_login_attempts (int, default 0)
- last_login_at (timestamp, nullable)
- last_logout_at (timestamp, nullable)
- phone_number (varchar, nullable)
- identification_number (varchar, nullable)
- created_at (timestamp, auto-generated)
- updated_at (timestamp, auto-updated)

**RevokedToken**
- id (PK, auto-increment)
- token (unique, not null)
- revoked_at (timestamp)
- expires_at (timestamp) — when the original JWT expires

**PasswordResetToken**
- id (PK, auto-increment)
- token (not null)
- email (not null)
- expiry_time (timestamp, not null) — 5 minutes from generation
- created_at (timestamp)

**LoginAuditLog**
- id (PK, auto-increment)
- email (not null)
- status (not null: SUCCESS, FAILED_INVALID_CREDENTIALS, ACCOUNT_LOCKED, ACCOUNT_DISABLED)
- ip_address (varchar, nullable)
- user_agent (varchar, nullable)
- created_at (timestamp, auto-generated)

---

## 6. Error Handling

THE system SHALL handle all unhandled exceptions via global @RestControllerAdvice handler.

WHERE validation fails (missing required fields, invalid format), THE system SHALL return HTTP 400 Bad Request with field-level error messages.

WHERE user email is already in use, THE system SHALL return HTTP 400 Bad Request with message "Email already exists".

WHERE user credentials are invalid (wrong password or user not found), THE system SHALL return HTTP 401 Unauthorized with generic message "INVALID_CREDENTIALS".

WHERE account is LOCKED or INACTIVE, THE system SHALL return HTTP 401 Unauthorized with message "ACCOUNT_LOCKED" or "ACCOUNT_DISABLED".

WHERE JWT is expired, THE system SHALL return HTTP 401 Unauthorized with message "SESSION_EXPIRED".

WHERE JWT is invalid or malformed, THE system SHALL return HTTP 401 Unauthorized with message "INVALID_TOKEN".

WHERE JWT is revoked (user logged out), THE system SHALL return HTTP 401 Unauthorized with message "TOKEN_REVOKED".

WHERE user lacks required role for an endpoint, THE system SHALL return HTTP 403 Forbidden with message "Access denied".

WHERE requested resource is not found, THE system SHALL return HTTP 404 Not Found.

THE system SHALL NOT expose stack traces, SQL error messages, or internal class names in API responses.

THE system SHALL log full error details (including stack traces) server-side for debugging while returning friendly messages to clients.

Standard error response format:
```json
{
  "timestamp": "2026-07-14T10:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "INVALID_CREDENTIALS",
  "path": "/api/v1/auth/login"
}
```

---

## 7. Out of Scope

THE system SHALL NOT implement email verification workflows in Phase 1.

THE system SHALL NOT implement two-factor authentication (2FA).

THE system SHALL NOT support social login via Facebook, GitHub, or other providers beyond Google OAuth (which is a future enhancement).

THE system SHALL NOT provide API key authentication (JWT-only model).

THE system SHALL NOT implement passwordless authentication.

THE system SHALL NOT handle account recovery via SMS.

THE system SHALL NOT provide password strength meter or complexity rules beyond minimum 8 characters.

---

## User Scenarios & Testing

### User Story 1 — Register a new account (UC-01) (Priority: P1)
As a Guest, I want to register a new account using my email and password, so I can access the hotel booking system.

**Why this priority**: Registration is the entry point for all customer interactions in the system.
**Independent Test**: Send `POST /api/v1/auth/register` with a valid email, password (≥ 8 chars), and full name. Verify the response contains `userId`, `email`, and the message `"User registered successfully"`.

**Acceptance Scenarios**:
1. **Given** a guest provides a unique email and valid password, **When** registering, **Then** the account is created with role `CUSTOMER`, status `ACTIVE`, and the password is stored as a BCrypt hash.
2. **Given** a guest provides an email that already exists, **When** registering, **Then** registration is rejected with HTTP 409 Conflict.
3. **Given** a guest provides a password shorter than 8 characters, **When** registering, **Then** validation fails with HTTP 400 Bad Request.

---

### User Story 2 — Login to the system (UC-02) (Priority: P1)
As a registered user, I want to log in with my email and password, so I can receive a JWT token and access protected resources.

**Why this priority**: Login is a prerequisite for all authenticated operations.
**Independent Test**: Send `POST /api/v1/auth/login` with valid credentials. Verify the response contains a valid `accessToken` and `refreshToken`.

**Acceptance Scenarios**:
1. **Given** a user provides correct credentials, **When** logging in, **Then** an access token (24h) and refresh token (7 days) are returned, and a login audit log is saved with IP and User-Agent.
2. **Given** a user provides a wrong password, **When** logging in, **Then** `failedLoginAttempts` is incremented and HTTP 401 is returned.
3. **Given** a user has failed login 5 or more times, **When** attempting another login, **Then** the account status is set to `LOCKED` and HTTP 401 is returned with message `ACCOUNT_LOCKED`.
4. **Given** a user account is `LOCKED` or `INACTIVE`, **When** logging in, **Then** HTTP 401 is returned immediately without checking the password.

---

### User Story 3 — Logout (UC-03) (Priority: P2)
As an authenticated user, I want to log out, so my current JWT token is invalidated and cannot be reused.

**Why this priority**: Security requirement to prevent token misuse after logout.
**Independent Test**: Login to get a token, then call `POST /api/v1/auth/logout` with the `Authorization: Bearer {token}` header. Try using the same token again and verify it returns HTTP 401.

**Acceptance Scenarios**:
1. **Given** a user sends a valid JWT, **When** logging out, **Then** the token is added to the revoked token store and future requests using that token are rejected.

---

### User Story 4 — Update User Profile (UC-05) (Priority: P2)
As an authenticated user, I want to update my profile information (full name, email, phone number, identification number), so I can keep my account details current.

**Why this priority**: Required for customers to manage their personal information before booking.
**Independent Test**: Send `PUT /api/v1/users/me/profile` with updated `fullName`, `email`, `phoneNumber`. Verify the response returns the updated profile fields.

**Acceptance Scenarios**:
1. **Given** an authenticated user provides valid profile data, **When** updating profile, **Then** the profile is saved and the updated data is returned.
2. **Given** a user tries to update their email to one already used by another account, **When** submitting, **Then** the update is rejected with a validation error.

---

### User Story 5 — Forgot & Reset Password (UC-32) (Priority: P2)
As a Guest, I want to request a password reset link, so I can regain access to my account if I forget my password.

**Why this priority**: Essential for user account recovery.
**Independent Test**: Send `POST /api/v1/password/forgot-password` with a registered email. Verify a password reset token is generated. Then send `POST /api/v1/password/reset-password` with the token and new password. Verify the password is updated.

**Acceptance Scenarios**:
1. **Given** a user submits a registered email, **When** requesting password reset, **Then** a reset token is generated and stored with a 5-minute expiry.
2. **Given** a valid reset token, **When** submitting a new password, **Then** the password is updated as a new BCrypt hash and the token is invalidated.
3. **Given** an expired or invalid reset token, **When** submitting a new password, **Then** the request is rejected with an error.

---

### User Story 6 — Admin User Management (UC-23) (Priority: P2)
As an Admin, I want to view, create, update, lock, and unlock user accounts, so I can manage system access.

**Why this priority**: Required for operational control over the user base.
**Independent Test**: Call `GET /api/v1/admin/users` as Admin and verify paginated user list is returned. Call `PATCH /api/v1/admin/users/{id}/status` with `status: LOCKED` and verify the target user can no longer log in.

**Acceptance Scenarios**:
1. **Given** an Admin calls the user list endpoint, **When** requesting all users, **Then** a paginated list of users is returned.
2. **Given** an Admin locks a user account, **When** that user tries to log in, **Then** login is rejected with `ACCOUNT_LOCKED`.
3. **Given** a non-Admin user calls admin endpoints, **When** the request is processed, **Then** HTTP 403 Forbidden is returned.

---

---

## Acceptance Criteria

THE system SHALL pass all of the following criteria before release:

SC-001: All password hashes in the database SHALL be in BCrypt format. Plaintext passwords SHALL NOT exist.

SC-002: An account SHALL be automatically locked after exactly 5 consecutive failed login attempts. Further login attempts SHALL fail immediately without password verification.

SC-003: A revoked JWT token SHALL be rejected on subsequent requests with HTTP 401 Unauthorized. The rejected token SHALL NOT grant access to protected resources.

SC-004: Password reset tokens older than 5 minutes from generation SHALL be rejected with HTTP 400 Bad Request.

SC-005: Only users with role = ADMIN SHALL access /api/v1/admin/** endpoints. All other users SHALL receive HTTP 403 Forbidden.

SC-006: Every login attempt SHALL generate a record in login_audit_logs table, including success and all failure scenarios (wrong password, account locked, account disabled).


