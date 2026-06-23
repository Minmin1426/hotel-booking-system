# Feature Specification: 001-authentication-identity

**Feature Branch**: `001-authentication-identity`
**Created**: 2026-06-23
**Status**: Completed

## User Scenarios & Testing

### User Story 1 - User Registration and Authentication (Priority: P1)
As a Guest, I want to register a new account and login using my credentials to access booking features.

**Why this priority**: Core entry point for any authenticated action.
**Independent Test**: Register a guest via `/api/v1/auth/register`, then login via `/api/v1/auth/login` to receive a valid JWT token.

**Acceptance Scenarios**:
1. **Given** a guest with email `test@example.com`, **When** registering with a valid 8-character password, **Then** an account is created and HTTP 200 OK is returned.
2. **Given** a registered user, **When** logging in with correct credentials, **Then** a JWT Access Token (expires <= 24h) and a Refresh Token are returned.
3. **Given** a registered user, **When** logging in with incorrect credentials 5 times consecutively, **Then** the account is locked and subsequent logins fail.

### User Story 2 - Profile Management & Logout (Priority: P2)
As an authenticated Customer, I want to view/update my profile and logout securely.

**Why this priority**: Allows user data updates and secure session termination.
**Independent Test**: Update profile details and logout, verifying that the logged-out JWT token is blacklisted.

**Acceptance Scenarios**:
1. **Given** an authenticated user, **When** calling `/api/v1/users/profile` (GET), **Then** their profile details are returned.
2. **Given** an authenticated user, **When** logging out, **Then** the JWT token is invalidated and cannot be reused.

### User Story 3 - Admin User Management (Priority: P3)
As an Admin, I want to manage user statuses (Lock/Unlock) to secure the system.

**Why this priority**: Essential for administration and moderating suspicious accounts.
**Independent Test**: Lock a user account as admin and verify the user can no longer log in.

**Acceptance Scenarios**:
1. **Given** an authenticated Admin, **When** updating a user's status to `LOCKED`, **Then** the user's status is saved and they are blocked from logging in.

## Requirements

### Functional Requirements
- **FR-001**: Guest registration must validate email format and verify password length >= 8.
- **FR-002**: Password must be stored as a BCrypt hash with strength >= 12.
- **FR-003**: JWT must expire in <= 24 hours. Refresh token rotation is required.
- **FR-004**: System must track failed login attempts and lock the account after 5 consecutive failures.
- **FR-005**: Logged-out tokens must be placed in a database-backed token blacklist.

### Key Entities
- **User**: Represents a registered user. Fields: `userId`, `email`, `passwordHash`, `fullName`, `role` (CUSTOMER, STAFF, ADMIN, DIRECTOR), `status` (ACTIVE, LOCKED), `failedLoginAttempts`, `lastLoginAt`, `lastLogoutAt`.
- **LoginAuditLog**: Records login attempts for security auditing.

## Success Criteria
- **SC-001**: Guests can register and login successfully in less than 500ms response time.
- **SC-002**: Standardized JSON error format returned on validation or authentication failure.
- **SC-003**: 100% of passwords saved in database are secure BCrypt hashes.
