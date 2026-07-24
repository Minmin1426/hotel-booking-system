# Feature Specification: 007-customer-portal-profile

**Feature Branch**: `007-customer-portal-profile`
**Created**: 2026-07-23
**Status**: Draft
**Primary Actor(s)**: Customer, CorporateMember, Admin
**Related Use Cases**: UC-01 (extended), UC-05 (extended), SCR-101, SCR-102

---

## 1. Context & Goal

The Customer Portal Profile module extends the existing `User` entity and authentication flow to support both individual customers and corporate members. It provides enriched profile management, account type differentiation (CUSTOMER vs CORPORATE_MEMBER), corporate tax profile (CTP) verification for invoicing, and Google Sign-In integration. This module is the foundation for all downstream customer-facing features (wallet, loyalty, vouchers, meal tickets).

**Goal**: Enable individual and corporate customers to manage their profiles, authenticate via Google, and have their tax/company information verified before receiving corporate invoices.

---

## 2. Actors & Roles

**Customer** (role = CUSTOMER): Authenticated individual user
- Views and updates own profile (fullName, email, phoneNumber, identificationNumber)
- Links/unlinks Google account to existing account
- Views account type and membership tier

**CorporateMember** (role = CORPORATE_MEMBER): Authenticated corporate user
- All Customer permissions, plus:
- Manages corporate tax profile (companyName, taxCode, companyAddress, billingEmail)
- Requests CTP verification (status: PENDING → VERIFIED / REJECTED)
- Views booking history with corporate invoice eligibility flag

**Admin** (role = ADMIN): System administrator
- Reviews CTP verification requests
- Approves or rejects corporate tax profiles
- Can reset CTP verification status

**Guest**: Unauthenticated visitor
- Registers new account (individual or corporate)
- Initiates Google Sign-In
- Cannot access any protected endpoint

---

## 3. Functional Requirements

### FR-001: Account Type on Registration
THE system SHALL support two account types during registration: `CUSTOMER` and `CORPORATE_MEMBER`.
THE system SHALL store account type in the `account_type` column of the `users` table.
THE system SHALL default to `CUSTOMER` when `accountType` is not specified in the registration request.

### FR-002: Corporate Tax Profile (CTP)
THE system SHALL store for CORPORATE_MEMBER users: `company_name`, `tax_code`, `company_address`, `billing_email`.
THE system SHALL have a CTP verification status: `NOT_SUBMITTED`, `PENDING`, `VERIFIED`, `REJECTED`.
THE system SHALL default CTP verification status to `NOT_SUBMITTED` on account creation.

### FR-003: CTP Verification Workflow
WHEN a CorporateMember submits a CTP, THE system SHALL set status to `PENDING`.
WHEN an Admin approves the CTP, THE system SHALL set status to `VERIFIED` and record `verified_at` and `verified_by`.
WHEN an Admin rejects the CTP, THE system SHALL set status to `REJECTED` with an optional rejection reason.
WHERE CTP status is not `VERIFIED`, THE system SHALL NOT allow corporate invoice generation.

### FR-004: Google Sign-In Enhancement
WHEN a user logs in with Google and the email matches an existing `CUSTOMER` or `CORPORATE_MEMBER` account, THE system SHALL link the Google token and return existing account tokens.
WHEN a user logs in with Google and no account exists, THE system SHALL create a new `CUSTOMER` account with role `CUSTOMER`.
THE system SHALL store the Google `sub` (subject identifier) in `google_subject_id` column.
THE system SHALL allow multiple Google tokens per account (token refresh scenarios) without creating duplicates.

### FR-005: Profile Update
THE system SHALL validate that a new email is not already in use by another account (same as existing UC-05).
THE system SHALL allow CorporateMembers to update their corporate tax profile fields.
THE system SHALL reset CTP verification status to `PENDING` when any CTP field is modified after initial `VERIFIED` status.

### FR-006: CTP Verification Audit
THE system SHALL record every CTP status change in `ctp_audit_logs` with: userId, adminId, previousStatus, newStatus, reason, timestamp.

---

## 4. Data Model

### Extended User Entity
| Field | Type | Description |
|---|---|---|
| account_type | VARCHAR | `CUSTOMER` (default) or `CORPORATE_MEMBER` |
| google_subject_id | VARCHAR | Google OAuth `sub` claim, nullable |
| company_name | VARCHAR | Corporate company name, nullable |
| tax_code | VARCHAR | Tax identification number, nullable |
| company_address | TEXT | Corporate billing address, nullable |
| billing_email | VARCHAR | Corporate billing email, nullable |
| ctp_status | VARCHAR | `NOT_SUBMITTED`, `PENDING`, `VERIFIED`, `REJECTED` |
| ctp_verified_at | TIMESTAMP | When CTP was verified, nullable |
| ctp_verified_by | BIGINT | Admin user who verified, FK to users, nullable |

### CtpAuditLog
| Field | Type | Description |
|---|---|---|
| id | BIGINT | PK, auto-increment |
| user_id | BIGINT | FK to users |
| admin_id | BIGINT | FK to users (who made the change) |
| previous_status | VARCHAR | Status before change |
| new_status | VARCHAR | Status after change |
| reason | TEXT | Reason for change (especially for rejection) |
| created_at | TIMESTAMP | Auto-generated |

---

## 5. API Contracts

### Registration with Account Type
```
POST /api/v1/auth/register
```
- **Request**: `{ "fullName", "email", "password", "phoneNumber", "accountType": "CUSTOMER" | "CORPORATE_MEMBER" }`
- **Response 201**: `{ "userId", "email", "fullName", "accountType", "message" }`

### Corporate Tax Profile Submission
```
PUT /api/v1/users/me/corporate-profile
```
- **Auth**: Bearer token (CORPORATE_MEMBER)
- **Request**: `{ "companyName", "taxCode", "companyAddress", "billingEmail" }`
- **Response 200**: `{ "companyName", "taxCode", "ctpStatus": "PENDING", "message" }`

### Get Corporate Tax Profile
```
GET /api/v1/users/me/corporate-profile
```
- **Auth**: Bearer token (CORPORATE_MEMBER)
- **Response 200**: `{ "companyName", "taxCode", "companyAddress", "billingEmail", "ctpStatus", "verifiedAt" }`

### Admin: List Pending CTP Verifications
```
GET /api/v1/admin/ctp-verifications?status=PENDING&page=0&size=20
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: Paginated list of CORPORATE_MEMBER users with CTP status

### Admin: Approve CTP
```
POST /api/v1/admin/ctp-verifications/{userId}/approve
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: `{ "userId", "ctpStatus": "VERIFIED", "message" }`

### Admin: Reject CTP
```
POST /api/v1/admin/ctp-verifications/{userId}/reject
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "reason": "Tax code not found in government registry" }`
- **Response 200**: `{ "userId", "ctpStatus": "REJECTED", "message" }`

### Google Sign-In
```
POST /api/v1/auth/google
```
- **Request**: `{ "token": "<google-id-token-or-access-token>" }`
- **Response 200**: `{ "accessToken", "refreshToken", "email", "role", "accountType", "isNewUser" }`

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Email already exists on register | 409 Conflict | EMAIL_ALREADY_EXISTS |
| Invalid Google token | 401 Unauthorized | INVALID_SOCIAL_TOKEN |
| CTP already verified — modification attempted | 400 Bad Request | CTP_ALREADY_VERIFIED |
| Non-corporate user accesses CTP endpoints | 403 Forbidden | ACCESS_DENIED |
| Admin approving already-verified CTP | 400 Bad Request | CTP_ALREADY_VERIFIED |
| CTP field validation failure | 400 Bad Request | Field-level validation errors |

---

## 7. User Scenarios & Testing

### US-1: Individual Registration (Priority: P1)
As a Guest, I want to register as an individual customer, so I can book hotels.

**Given** a guest provides valid registration data with `accountType = CUSTOMER`
**When** submitting `POST /api/v1/auth/register`
**Then** account is created with `role = CUSTOMER`, `account_type = CUSTOMER`, `ctp_status = NOT_SUBMITTED`

### US-2: Corporate Registration (Priority: P1)
As a Guest, I want to register as a corporate member, so I can book hotels on behalf of my company and receive corporate invoices.

**Given** a guest provides valid registration data with `accountType = CORPORATE_MEMBER`
**When** submitting `POST /api/v1/auth/register`
**Then** account is created with `role = CUSTOMER` (login role), `account_type = CORPORATE_MEMBER`, `ctp_status = NOT_SUBMITTED`

### US-3: Google Sign-In — New User (Priority: P1)
As a Guest, I want to log in with Google, so I can register and access the system without creating a password.

**Given** a valid Google token is submitted
**When** no existing account matches the Google email
**Then** a new `CUSTOMER` account is created with `google_subject_id` set, and JWT tokens are returned

### US-4: Google Sign-In — Existing User (Priority: P1)
As a Customer, I want to link my Google account, so I can log in faster next time.

**Given** a valid Google token matches an existing `CUSTOMER` account
**When** submitting `POST /api/v1/auth/google`
**Then** JWT tokens are returned and `google_subject_id` is updated/confirmed

### US-5: Submit Corporate Tax Profile (Priority: P1)
As a CorporateMember, I want to submit my company's tax information, so I can receive verified corporate invoices.

**Given** an authenticated CorporateMember
**When** submitting `PUT /api/v1/users/me/corporate-profile` with valid company data
**Then** CTP is saved with `ctp_status = PENDING`, audit log created

### US-6: Admin Approves CTP (Priority: P1)
As an Admin, I want to approve corporate tax profiles, so verified companies can receive invoices.

**Given** a CORPORATE_MEMBER has `ctp_status = PENDING`
**When** Admin calls `POST /api/v1/admin/ctp-verifications/{userId}/approve`
**Then** `ctp_status = VERIFIED`, `ctp_verified_at` and `ctp_verified_by` are set, audit log created

### US-7: CTP Modification After Verification (Priority: P2)
As a CorporateMember, I want to update my company details after initial verification, so I can keep information current.

**Given** a CorporateMember has `ctp_status = VERIFIED`
**When** submitting updated CTP data
**Then** CTP status is reset to `PENDING`, audit log recorded with previous status `VERIFIED`

### US-8: CTP Rejection with Reason (Priority: P2)
As an Admin, I want to reject CTP submissions with a reason, so corporate members know what to fix.

**Given** a CORPORATE_MEMBER has `ctp_status = PENDING`
**When** Admin calls `POST /api/v1/admin/ctp-verifications/{userId}/reject` with a reason
**Then** `ctp_status = REJECTED`, reason stored in audit log

---

## 8. Acceptance Criteria

AC-007: A guest can register as either `CUSTOMER` or `CORPORATE_MEMBER`. Default is `CUSTOMER` when unspecified.
AC-008: Google Sign-In creates a new `CUSTOMER` account for new users, or returns existing tokens for registered users.
AC-009: CorporateMember can submit CTP with status transitioning to `PENDING`.
AC-010: Admin can approve a `PENDING` CTP, setting it to `VERIFIED` with audit trail.
AC-011: Admin can reject a `PENDING` or `VERIFIED` CTP with a reason.
AC-012: Modifying CTP fields after `VERIFIED` status resets to `PENDING`.
AC-013: Non-admin users cannot access `/api/v1/admin/ctp-verifications/**`.
AC-014: Non-corporate users cannot access `/api/v1/users/me/corporate-profile`.

---

## 9. Out of Scope

- Actual invoice generation (handled in a separate billing/invoice module)
- Government tax code API verification (manual admin review only for Phase 1)
- Google workspace domain verification (allow any `@company.com` email for Phase 1)
- Email notifications on CTP approval/rejection (Phase 2)
- Corporate member tier levels (SCR-105 — Loyalty Program)
- Meal ticket wallet (SCR-107)
