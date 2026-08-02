# Implementation Plan: 007-customer-portal-profile

**Branch**: `007-customer-portal-profile` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Extend the User entity and authentication flow to support corporate member accounts with CTP (Corporate Tax Profile) verification, and enhance Google Sign-In with account linking. Includes admin CTP review workflow and full audit trail.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Security 6.3.0, Spring Data JPA
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL (Neon cloud) via Flyway migrations — `V20__Customer_portal_profile.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.user` (entities, services extended), `com.hotelbooking.auth` (Google login enhanced)
- **New Package**: `com.hotelbooking.user.ctp` — CtpAuditLog entity, CtpService/CtpServiceImpl, CtpController
- **DTOs**: All responses use DTOs — JPA entities never returned directly
- **No business logic in controllers**: All rules enforced in `CtpServiceImpl`, `UserServiceImpl`
- **New Role**: `CORPORATE_MEMBER` — login role is still `CUSTOMER` (Spring Security authority `ROLE_CUSTOMER`), but `account_type = CORPORATE_MEMBER`

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V20__Customer_portal_profile.sql`
  - Add `account_type` column to `users` (default `'CUSTOMER'`)
  - Add `google_subject_id` column to `users`
  - Add CTP columns: `company_name`, `tax_code`, `company_address`, `billing_email`, `ctp_status`, `ctp_verified_at`, `ctp_verified_by`
  - Create `ctp_audit_logs` table

### Source Code

#### Extended User Entity
- `src/main/java/com/hotelbooking/user/User.java`
  - Add fields: `accountType`, `googleSubjectId`, `companyName`, `taxCode`, `companyAddress`, `billingEmail`, `ctpStatus`, `ctpVerifiedAt`, `ctpVerifiedBy`
  - Add helper methods: `isCorporateMember()`, `isCtpVerified()`

#### New Entities
- `src/main/java/com/hotelbooking/user/ctp/CtpAuditLog.java` — Entity with all audit fields
- `src/main/java/com/hotelbooking/user/ctp/CtpAuditLogRepository.java`

#### DTOs (in existing or new dto packages)
- `src/main/java/com/hotelbooking/user/dto/RegisterRequest.java` — Add `accountType` field
- `src/main/java/com/hotelbooking/user/dto/UserProfileResponse.java` — Add `accountType`, `companyName`, `ctpStatus`
- `src/main/java/com/hotelbooking/auth/dto/LoginResponse.java` — Add `accountType`, `isNewUser`
- `src/main/java/com/hotelbooking/user/dto/CorporateProfileRequest.java` — CTP submission request
- `src/main/java/com/hotelbooking/user/dto/CorporateProfileResponse.java` — CTP response
- `src/main/java/com/hotelbooking/user/dto/CtpVerificationSummary.java` — Admin list item

#### Services
- `src/main/java/com/hotelbooking/user/UserServiceImpl.java` — Extend `register()` to handle `accountType`, extend `updateProfile()` to handle CTP
- `src/main/java/com/hotelbooking/auth/AuthServiceImpl.java` — Extend `loginWithGoogle()` to store `googleSubjectId`, add `isNewUser` flag
- `src/main/java/com/hotelbooking/user/ctp/CtpService.java` — Interface
- `src/main/java/com/hotelbooking/user/ctp/CtpServiceImpl.java` — Submit CTP, approve, reject, list pending, audit log

#### Controllers
- `src/main/java/com/hotelbooking/user/UserController.java` — Add `PUT /me/corporate-profile`, `GET /me/corporate-profile`
- `src/main/java/com/hotelbooking/user/AdminUserController.java` — Add CTP verification endpoints
- `src/main/java/com/hotelbooking/auth/AuthController.java` — `LoginResponse` updated with new fields

### Testing
- `src/test/java/com/hotelbooking/user/CustomerPortalServiceTest.java` — Unit tests for CtpServiceImpl and extended UserService
- `src/test/java/com/hotelbooking/user/CustomerPortalControllerTest.java` — Controller tests for CTP endpoints with RBAC

## Non-Functional Requirements
- **Security**: CTP endpoints restricted to CORPORATE_MEMBER role. Admin CTP endpoints restricted to ADMIN role. All inputs validated via Bean Validation.
- **Audit**: Every CTP status change creates an immutable audit log entry.
- **Performance**: CTP listing uses pagination (max 20 per page).
- **Data Integrity**: Transactional — CTP update and audit log in same transaction.
