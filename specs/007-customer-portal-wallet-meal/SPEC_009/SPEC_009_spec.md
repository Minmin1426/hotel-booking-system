# Feature Specification: 009-refund-policy-engine

**Feature Branch**: `009-refund-policy-engine`
**Created**: 2026-07-23
**Status**: Approved & Completed
**Primary Actor(s)**: System, Customer, CorporateMember, Admin, Receptionist
**Related Use Cases**: SCR-108, UC-06 (booking cancellation)

---

## 1. Context & Goal

The current booking cancellation flow (from spec 003-booking-management) triggers a refund request but lacks a configurable, transparent refund policy. This module introduces a rule-based refund policy engine that calculates refund eligibility and amounts based on how far in advance the cancellation occurs relative to the check-in date. The engine also addresses the existing `PaymentServiceTest.testRetryFailedRefunds` NPE bug (null `refundAmount` causing crashes) and provides an admin-configurable policy that can be changed without code deployment.

**Goal**: Provide transparent, configurable cancellation refunds (100% / 50% / 0%) based on timing. Ensure graceful handling of null/edge cases. Fix the existing NPE in refund retry logic.

---

## 2. Actors & Roles

**System**: Automatically triggers refund calculation when a confirmed booking is cancelled.
**Customer / CorporateMember**: Can cancel their own bookings; sees refund amount before confirmation.
**Admin / Receptionist**: Can override refund policy for special cases, view refund history.
**Finance**: Can view refund audit logs and reports.

---

## 3. Functional Requirements

### FR-001: Configurable Refund Policy Rules
THE system SHALL support configurable refund policy rules stored in the `refund_policies` table.
THE system SHALL evaluate rules in order of `priority` (lower number = higher priority).
EACH rule SHALL define: `days_before_checkin` (threshold), `refund_percentage` (0–100), `description`.
THE system SHALL apply the first matching rule (smallest `days_before_checkin` that is <= actual days remaining).

### FR-002: Default Policy (Configurable)
THE default policy (no matching rule found) SHALL be configurable via `default_refund_percentage`.
SUGGESTED default rules:
| Days Before Check-In | Refund % | Description |
|---|---|---|
| ≥ 7 days | 100% | Full refund for early cancellations |
| 3–6 days | 50% | Partial refund for medium advance notice |
| < 3 days | 0% | No refund for late cancellations |
| 0 (past check-in) | 0% | No refund after check-in date |

### FR-003: Refund Calculation
WHEN a booking is cancelled, THE system SHALL calculate the refund amount as:
`refundAmount = payment.amount × refundPercentage / 100`
WHERE `payment.refundAmount` is null, THE system SHALL use `payment.amount` as the base.
THE system SHALL never refund more than the original payment amount.
WHERE payment status is not `SUCCESS`, THE system SHALL NOT initiate a refund.

### FR-004: Booking Cancellation Triggers Refund
WHEN a `CONFIRMED` booking is cancelled, THE system SHALL:
1. Calculate refund amount using the policy engine
2. Set `Payment.refundAmount` to the calculated value (not null)
3. Set `Payment.status` to `REFUND_PENDING`
4. Record a `RefundAuditLog` entry
5. Trigger the existing refund retry scheduler

### FR-005: Pre-Cancellation Refund Preview
THE system SHALL provide a refund preview endpoint so users see the refund amount before confirming cancellation.
`GET /api/v1/bookings/{bookingId}/refund-preview`
- Returns: `{ "originalAmount", "refundPercentage", "refundAmount", "policyDescription", "daysRemaining" }`

### FR-006: Admin Override
AN Admin or Receptionist SHALL be able to override the calculated refund percentage when cancelling on behalf of a customer.
`POST /api/v1/admin/bookings/{bookingId}/cancel` with `refundOverride` field

### FR-007: Refund Audit Trail
THE system SHALL record every refund calculation and status change in `refund_audit_logs`:
- `booking_id`, `payment_id`, `original_amount`, `refund_percentage`, `refund_amount`, `override_by` (nullable), `override_reason` (nullable), `previous_status`, `new_status`, `timestamp`

### FR-008: Null Safety in Refund Retry
WHERE `payment.getRefundAmount()` returns null, THE system SHALL fall back to `payment.getAmount()` before any arithmetic operations.
WHERE both `refundAmount` and `amount` are null, THE system SHALL log an error and skip the retry with status `MANUAL_REFUND_REQUIRED`.

---

## 4. Data Model

### RefundPolicy
| Field | Type | Description |
|---|---|---|
| policy_id | BIGINT | PK |
| name | VARCHAR | Policy rule name |
| days_before_checkin | INT | Threshold in days |
| refund_percentage | DECIMAL(5,2) | Percentage to refund (0.00–100.00) |
| description | VARCHAR | Human-readable description |
| priority | INT | Evaluation order (lower = first) |
| is_active | BOOLEAN | Whether rule is in effect |
| created_at | TIMESTAMP | Auto-generated |
| updated_at | TIMESTAMP | Auto-updated |

### RefundAuditLog
| Field | Type | Description |
|---|---|---|
| audit_id | BIGINT | PK |
| booking_id | BIGINT | FK to bookings |
| payment_id | BIGINT | FK to payments |
| original_amount | DECIMAL(18,2) | Original payment amount |
| refund_percentage | DECIMAL(5,2) | Applied refund percentage |
| refund_amount | DECIMAL(18,2) | Calculated refund amount |
| override_by | BIGINT | FK to users (admin who overrode), nullable |
| override_reason | TEXT | Reason for override, nullable |
| previous_payment_status | VARCHAR | Payment status before |
| new_payment_status | VARCHAR | Payment status after |
| policy_id | BIGINT | FK to RefundPolicy used |
| created_at | TIMESTAMP | Auto-generated |

---

## 5. API Contracts

### Get Refund Preview
```
GET /api/v1/bookings/{bookingId}/refund-preview
```
- **Auth**: Bearer token (booking owner or ADMIN)
- **Response 200**: `{ "bookingId", "originalAmount", "refundPercentage", "refundAmount", "policyName", "policyDescription", "daysRemaining" }`

### Cancel Booking (User-Initiated)
```
POST /api/v1/bookings/{bookingId}/cancel
```
- **Auth**: Bearer token (booking owner)
- **Response 200**: `{ "bookingId", "status": "CANCELLED", "refundAmount", "refundPercentage", "message" }`

### Admin Cancel with Override
```
POST /api/v1/admin/bookings/{bookingId}/cancel
Content-Type: application/json
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Request**: `{ "reason": "Customer requested extension", "refundOverride": 75.00 }` (optional override)
- **Response 200**: `{ "bookingId", "status": "CANCELLED", "refundAmount", "overrideApplied": true }`

### Admin: Configure Refund Policy
```
POST /api/v1/admin/refund-policies
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "name", "daysBeforeCheckin", "refundPercentage", "description", "priority", "isActive" }`

### Admin: List Refund Policies
```
GET /api/v1/admin/refund-policies
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: List of all RefundPolicy records

### Admin: Update Refund Policy
```
PUT /api/v1/admin/refund-policies/{policyId}
```

### Admin: View Refund Audit Logs
```
GET /api/v1/admin/refund-audit-logs?bookingId=&paymentId=&page=0&size=20
```
- **Auth**: Bearer token (ADMIN or FINANCE)
- **Response 200**: Paginated RefundAuditLog entries

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Cancelling non-confirmed booking | 400 Bad Request | BOOKING_NOT_CONFIRMED |
| Cancelling already-cancelled booking | 400 Bad Request | BOOKING_ALREADY_CANCELLED |
| No payment found for booking | 400 Bad Request | NO_PAYMENT_FOUND |
| Payment already refunded | 400 Bad Request | PAYMENT_ALREADY_REFUNDED |
| Override percentage > 100 | 400 Bad Request | INVALID_REFUND_PERCENTAGE |
| Booking not found | 404 Not Found | RESOURCE_NOT_FOUND |
| Policy not found | 404 Not Found | RESOURCE_NOT_FOUND |

---

## 7. User Scenarios & Testing

### US-1: Full Refund — 7+ Days Before Check-In (Priority: P1)
As a Customer, I want to cancel my booking 10 days before check-in, so I receive a 100% refund.

**Given** Booking is `CONFIRMED`, payment is `SUCCESS` for 1,000,000 VND
**When** I cancel 10 days before check-in
**Then** Refund percentage = 100%, refund amount = 1,000,000 VND, Payment status → `REFUND_PENDING`, `RefundAuditLog` created

### US-2: Partial Refund — 3–6 Days Before (Priority: P1)
As a Customer, I want to cancel 4 days before check-in, so I receive a 50% refund.

**Given** Booking is `CONFIRMED`, payment is `SUCCESS` for 1,000,000 VND
**When** I cancel 4 days before check-in
**Then** Refund percentage = 50%, refund amount = 500,000 VND

### US-3: No Refund — Less Than 3 Days (Priority: P1)
As a Customer, I want to understand why I get no refund when cancelling 2 days before, so the policy is transparent.

**Given** Booking is `CONFIRMED`, payment is `SUCCESS`
**When** I cancel 2 days before check-in
**Then** Refund percentage = 0%, refund amount = 0, Payment status → `CANCELLED` (no refund needed)

### US-4: Refund Preview Before Cancellation (Priority: P1)
As a Customer, I want to see my refund amount before cancelling, so I can decide whether to proceed.

**Given** I own a `CONFIRMED` booking
**When** I call `GET /api/v1/bookings/{id}/refund-preview`
**Then** I see the exact refund amount and which policy rule applies

### US-5: Admin Override (Priority: P2)
As an Admin, I want to approve a 75% refund for a customer cancelling 2 days before check-in due to an emergency, so I can be fair.

**Given** Default policy says 0% refund for 2 days before
**When** Admin cancels the booking with `refundOverride = 75.00`
**Then** Refund amount = 75% of payment, override reason stored in audit log

### US-6: Null RefundAmount Fix in Retry (Priority: P1 — Bug Fix)
As the System, I want the refund retry job to handle null `refundAmount` gracefully, so it does not crash.

**Given** A Payment with `status = REFUND_PENDING`, `amount = 500,000` but `refundAmount = null`
**When** `retryFailedRefunds()` is called
**Then** The system uses `amount` as the base for the refund calculation instead of throwing NPE

### US-7: Manual Refund Required (Priority: P2)
As the System, I want to escalate to manual review when both `amount` and `refundAmount` are null, so no refunds are silently lost.

**Given** A Payment with `status = REFUND_PENDING` and both `amount` and `refundAmount` are null
**When** `retryFailedRefunds()` is called
**Then** Payment status → `MANUAL_REFUND_REQUIRED`, error logged

---

## 8. Acceptance Criteria

AC-021: Cancellations 7+ days before check-in result in 100% refund.
AC-022: Cancellations 3–6 days before check-in result in 50% refund.
AC-023: Cancellations < 3 days before check-in result in 0% refund.
AC-024: The refund preview endpoint shows the exact refund amount and applicable policy.
AC-025: Admin can override the default refund percentage with a reason.
AC-026: Every refund calculation creates a `RefundAuditLog` entry.
AC-027: `retryFailedRefunds()` does not throw NPE when `refundAmount` is null — it uses `amount` as fallback.
AC-028: Refund policies are configurable via admin API without code changes.

---

## 9. Out of Scope

- Refund to original payment method (Stripe refund API integration — handled by existing `PaymentServiceImpl` retry logic)
- Partial refunds (one booking = one refund for Phase 1)
- Multiple payments per booking (one payment per booking per spec 003)
- Automatic email notification on refund initiation
- Finance dashboard with charts/graphs (Phase 2)
- Holiday/special event policy overrides (Phase 2)
