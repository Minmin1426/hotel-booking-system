# Tasks: 007-customer-portal-profile

**Input**: Design documents from `/specs/007-customer-portal-profile/`

## Phase 1: Database & Entity Extensions (Data Model)
- [x] T001 Create Flyway migration `V20__Customer_portal_profile.sql` — add columns to `users`: `account_type`, `google_subject_id`, `company_name`, `tax_code`, `company_address`, `billing_email`, `ctp_status`, `ctp_verified_at`, `ctp_verified_by`; create `ctp_audit_logs` table
- [x] T002 Extend `User.java` entity with new fields and helper methods: `isCorporateMember()`, `isCtpVerified()`
- [x] T003 Implement `CtpAuditLog` entity and `CtpAuditLogRepository`

## Phase 2: DTOs
- [x] T004 Update `RegisterRequest.java` — add `accountType` field (`CUSTOMER` / `CORPORATE_MEMBER`, default `CUSTOMER`)
- [x] T005 Update `RegisterResponse.java` — add `accountType` field
- [x] T006 Update `UserProfileResponse.java` — add `accountType`, `companyName`, `ctpStatus`, `ctpVerifiedAt`
- [x] T007 Update `LoginResponse.java` — add `accountType`, `isNewUser`
- [x] T008 Create `CorporateProfileRequest.java` — `companyName`, `taxCode`, `companyAddress`, `billingEmail` with validation
- [x] T009 Create `CorporateProfileResponse.java` — full CTP view with `ctpStatus`, `verifiedAt`
- [x] T010 Create `CtpVerificationSummary.java` — admin list item DTO
- [x] T011 Create `ApproveCtpRequest.java` — empty or with optional note
- [x] T012 Create `RejectCtpRequest.java` — `reason` field (required)

## Phase 3: Auth Service Enhancements
- [x] T013 Extend `AuthServiceImpl.register()` — handle `accountType` field, set default `CUSTOMER`
- [x] T014 Extend `AuthServiceImpl.loginWithGoogle()` — store `googleSubjectId` on first login, return `accountType` and `isNewUser` in `LoginResponse`
- [x] T015 Update `JwtService` if needed to include `accountType` in JWT claims

## Phase 4: CTP Service
- [x] T016 Implement `CtpService.submitProfile()` — save CTP, set status to `PENDING`, create audit log
- [x] T017 Implement `CtpService.getProfile()` — return CTP data for the authenticated CORPORATE_MEMBER
- [x] T018 Implement `CtpService.approveProfile()` — admin approval, set `VERIFIED`, record `verifiedAt`/`verifiedBy`, audit log
- [x] T019 Implement `CtpService.rejectProfile()` — admin rejection, set `REJECTED`, store reason, audit log
- [x] T020 Implement `CtpService.listVerifications()` — paginated list for admin with status filter

## Phase 5: Controller Endpoints
- [x] T021 Add `PUT /api/v1/users/me/corporate-profile` — secured with CORPORATE_MEMBER role check
- [x] T022 Add `GET /api/v1/users/me/corporate-profile` — secured with CORPORATE_MEMBER role check
- [x] T023 Add `GET /api/v1/admin/ctp-verifications` — secured with ADMIN role, paginated
- [x] T024 Add `POST /api/v1/admin/ctp-verifications/{userId}/approve` — secured with ADMIN role
- [x] T025 Add `POST /api/v1/admin/ctp-verifications/{userId}/reject` — secured with ADMIN role

## Phase 6: Error Handling & Edge Cases
- [x] T026 Add `CtpAlreadyVerifiedException` to `GlobalExceptionHandler` — 400 Bad Request
- [x] T027 Add `CtpNotFoundException` — 404 Not Found
- [x] T028 Handle CTP modification after `VERIFIED` — reset to `PENDING`
- [x] T029 Non-corporate users accessing CTP endpoints → 403 Forbidden

## Phase 7: Testing
- [x] T030 Write `CustomerPortalServiceTest.java` — unit tests for CtpServiceImpl and extended UserService: register CORPORATE_MEMBER, Google login new/existing user, CTP submit, approve, reject, reset to PENDING on modification
- [x] T031 Write `CustomerPortalControllerTest.java` — controller tests: unauthorized → 403, admin access → 200, non-corporate accessing CTP → 403
- [x] T032 Verify all new acceptance criteria (AC-007 to AC-014) are covered by tests
