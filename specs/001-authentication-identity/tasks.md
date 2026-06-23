# Tasks: 001-authentication-identity

**Input**: Design documents from `/specs/001-authentication-identity/`

## Phase 1: Foundational (Authentication Framework)
- [x] T001 Setup JWT filter and Security Config under `com.hotelbooking.common.security`
- [x] T002 Implement `BCryptPasswordEncoder` bean with strength 12 in `SecurityConfig`
- [x] T003 Setup DB migration for `users` and `login_audit_logs` tables

## Phase 2: User Story 1 - Registration & Authentication
- [x] T004 Create `User` JPA Entity and `UserRepository` under `com.hotelbooking.user`
- [x] T005 Create request/response DTOs: `RegisterRequest`, `LoginRequest`, `AuthResponse`
- [x] T006 Implement registration and login logic in `AuthServiceImpl` with failed login tracking
- [x] T007 Implement `/api/v1/auth/register` and `/api/v1/auth/login` endpoints in `AuthController`
- [x] T008 Add validation annotations `@NotBlank`, `@Size`, `@Email` on request DTOs
- [x] T009 Write unit tests in `AuthServiceImplTest` (verify registration constraints and account locking)

## Phase 3: User Story 2 - Profile Management & Logout
- [x] T010 Create `UserProfileResponse` DTO
- [x] T011 Implement `UserController` for GET `/api/v1/users/profile` and PUT profile updates
- [x] T012 Implement logout token blacklisting in `AuthServiceImpl`

## Phase 4: User Story 3 - Admin User Management
- [x] T013 Create `AdminUserController` and `AdminUserService`
- [x] T014 Implement user status toggle endpoint (`/api/v1/admin/users/{userId}/status`)
- [x] T015 Write unit tests in `AdminUserServiceTest` and controller integration tests in `AdminUserControllerTest`
