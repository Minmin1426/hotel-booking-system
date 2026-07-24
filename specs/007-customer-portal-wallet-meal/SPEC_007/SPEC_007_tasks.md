# Tasks: 007-customer-portal-profile

**Input**: Design documents from `/specs/007-customer-portal-profile/`

## Phase 1: Database & Entity Extensions (Data Model)
- [ ] T001 Create Flyway migration `V20__Customer_portal_profile.sql` — add columns to `users`: `account_type`, `google_subject_id`, `company_name`, `tax_code`, `company_address`, `billing_email`, `ctp_status`, `ctp_verified_at`, `ctp_verified_by`; create `ctp_audit_logs` table
- [ ] T002 Extend `User.java` entity with new fields and helper methods: `isCorporateMember()`, `isCtpVerified()`
- [ ] T003 Implement `CtpAuditLog` entity and `CtpAuditLogRepository`

## Phase 2: DTOs
- [ ] T004 Update `RegisterRequest.java` — add `accountType` field (`CUSTOMER` / `CORPORATE_MEMBER`, default `CUSTOMER`)
- [ ] T005 Update `RegisterResponse.java` — add `accountType` field
- [ ] T006 Update `UserProfileResponse.java` — add `accountType`, `companyName`, `ctpStatus`, `ctpVerifiedAt`
- [ ] T007 Update `LoginResponse.java` — add `accountType`, `isNewUser`
- [ ] T008 Create `CorporateProfileRequest.java` — `companyName`, `taxCode`, `companyAddress`, `billingEmail` with validation
- [ ] T009 Create `CorporateProfileResponse.java` — full CTP view with `ctpStatus`, `verifiedAt`
- [ ] T010 Create `CtpVerificationSummary.java` — admin list item DTO
- [ ] T011 Create `ApproveCtpRequest.java` — empty or with optional note
- [ ] T012 Create `RejectCtpRequest.java` — `reason` field (required)

## Phase 3: Auth Service Enhancements
- [ ] T013 Extend `AuthServiceImpl.register()` — handle `accountType` field, set default `CUSTOMER`
- [ ] T014 Extend `AuthServiceImpl.loginWithGoogle()` — store `googleSubjectId` on first login, return `accountType` and `isNewUser` in `LoginResponse`
- [ ] T015 Update `JwtService` if needed to include `accountType` in JWT claims

## Phase 4: CTP Service
- [ ] T016 Implement `CtpService.submitProfile()` — save CTP, set status to `PENDING`, create audit log
- [ ] T017 Implement `CtpService.getProfile()` — return CTP data for the authenticated CORPORATE_MEMBER
- [ ] T018 Implement `CtpService.approveProfile()` — admin approval, set `VERIFIED`, record `verifiedAt`/`verifiedBy`, audit log
- [ ] T019 Implement `CtpService.rejectProfile()` — admin rejection, set `REJECTED`, store reason, audit log
- [ ] T020 Implement `CtpService.listVerifications()` — paginated list for admin with status filter

## Phase 5: Controller Endpoints
- [ ] T021 Add `PUT /api/v1/users/me/corporate-profile` — secured with CORPORATE_MEMBER role check
- [ ] T022 Add `GET /api/v1/users/me/corporate-profile` — secured with CORPORATE_MEMBER role check
- [ ] T023 Add `GET /api/v1/admin/ctp-verifications` — secured with ADMIN role, paginated
- [ ] T024 Add `POST /api/v1/admin/ctp-verifications/{userId}/approve` — secured with ADMIN role
- [ ] T025 Add `POST /api/v1/admin/ctp-verifications/{userId}/reject` — secured with ADMIN role

## Phase 6: Error Handling & Edge Cases
- [ ] T026 Add `CtpAlreadyVerifiedException` to `GlobalExceptionHandler` — 400 Bad Request
- [ ] T027 Add `CtpNotFoundException` — 404 Not Found
- [ ] T028 Handle CTP modification after `VERIFIED` — reset to `PENDING`
- [ ] T029 Non-corporate users accessing CTP endpoints → 403 Forbidden

## Phase 7: Testing
- [ ] T030 Write `CustomerPortalServiceTest.java` — unit tests for CtpServiceImpl and extended UserService: register CORPORATE_MEMBER, Google login new/existing user, CTP submit, approve, reject, reset to PENDING on modification
- [ ] T031 Write `CustomerPortalControllerTest.java` — controller tests: unauthorized → 403, admin access → 200, non-corporate accessing CTP → 403
- [ ] T032 Verify all new acceptance criteria (AC-007 to AC-014) are covered by tests
