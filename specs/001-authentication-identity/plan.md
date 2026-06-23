# Implementation Plan: 001-authentication-identity

**Branch**: `001-authentication-identity` | **Date**: 2026-06-23 | **Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/001-authentication-identity/spec.md)

## Summary
Implement registration, login, logout, profile updates, and admin user management using JWT and BCrypt under a package-by-feature architecture.

## Technical Context
- **Language/Version**: Java 17
- **Primary Dependencies**: Spring Boot 3.3.0, Spring Security, JWT, Lombok, Jakarta Validation
- **Storage**: SQL Server
- **Testing**: JUnit 5, Mockito, MockMvc

## Constitution Check
- **Feature Package**: `com.hotelbooking.auth`, `com.hotelbooking.user`
- **Secrets Management**: Read secret keys from env vars (`jwt.secret=${JWT_SECRET}`). No hardcoded passwords or strings.
- **DTOs**: Mandated for requests/responses (e.g., `RegisterRequest`, `LoginRequest`, `UserProfileResponse`).

## Project Structure

### Source Code
- `src/main/java/com/hotelbooking/auth/`
  - [AuthController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/auth/AuthController.java)
  - [AuthService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/auth/AuthService.java)
  - [AuthServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/auth/AuthServiceImpl.java)
- `src/main/java/com/hotelbooking/user/`
  - [UserController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/UserController.java)
  - [AdminUserController.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/AdminUserController.java)
  - [UserService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/UserService.java)
  - [UserServiceImpl.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/UserServiceImpl.java)
  - [AdminUserService.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/AdminUserService.java)
  - [User.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/User.java) (JPA Entity)
  - [UserRepository.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/main/java/com/hotelbooking/user/UserRepository.java)

### Testing
- `src/test/java/com/hotelbooking/`
  - [AuthServiceImplTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/service/AuthServiceImplTest.java)
  - [AdminUserControllerTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/user/AdminUserControllerTest.java)
  - [AdminUserServiceTest.java](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/src/test/java/com/hotelbooking/user/AdminUserServiceTest.java)
