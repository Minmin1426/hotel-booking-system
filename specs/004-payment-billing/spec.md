# Feature Specification: 004-payment-billing

**Feature Branch:** `004-payment-billing`  
**Created:** 2026-06-23  
**Updated:** 2026-07-04  
**Status:** APPROVED  
**Priority:** HIGH (Financial & Security)  
**Specification Level:** Formal Specification

---

# 1. Business Context & Goals

## Business Context
Customers need a secure, reliable and auditable online payment process to complete hotel bookings. The system must automatically verify payment results received from the payment gateway before updating booking status while protecting against fraudulent or replayed webhook requests. Because payment operations directly affect revenue, customer trust, and legal compliance, this feature is classified as a high-risk core module.

## Goals
The system shall:
- Allow secure online payments via Stripe.
- Automatically confirm bookings after successful payment.
- Support voucher discounts and validations.
- Automatically process eligible refunds.
- Ensure complete traceability of every financial transaction.
- Guarantee data consistency across all services using ACID transactions.

## Success Metrics
- 100% webhook requests are authenticated via HMAC SHA-256.
- Duplicate webhook processing creates 0 duplicate transactions (Idempotency).
- Payment confirmation completes within 5 seconds.
- 100% payment events generate immutable audit logs.
- 0 successful Replay Attack requests.

---

# 2. Stakeholders & User Personas

## Customer
A customer books hotel rooms and completes payment through supported payment methods.
**Goals:** Complete payment quickly, apply discount vouchers, receive immediate booking confirmation, view payment history, and receive automated refunds when applicable.

## Administrator & Receptionist
Administrators manage payment records and investigate payment issues. Receptionists can handle manual/offline payments (Cash/Bank Transfer).
**Goals:** Monitor payment status, review failed transactions, process manual refund requests, manually confirm cash/bank payments, and access payment audit logs.

## Payment Gateway (Stripe)
Third-party payment provider that processes transactions.
**Responsibilities:** Process payments, send payment webhook callbacks, provide transaction status and intent verification.

---

# 3. User Scenarios (All Paths)

## US-001 — Successful Online Payment
As a Customer, I want to pay online via Stripe, so that my booking is confirmed immediately.
- **Given** Booking Status = PENDING
- **When** Valid Stripe webhook is received
- **Then** Payment Status = SUCCESS, Booking Status = CONFIRMED, Transaction ID is stored, PaymentAuditLog is created.

## US-002 — Manual Payment Confirmation
As a Receptionist, I want to manually confirm a cash or bank transfer payment, so that the system records the transaction and updates the booking to paid.
- **Given** Payment Method = CASH/BANK_TRANSFER, Booking Status = PENDING, Payment Status = PENDING
- **When** Staff calls `/api/payments/{paymentId}/confirm-cash` or `confirm-bank`
- **Then** Payment Status = SUCCESS, PaymentAuditLog is created, Confirmation email is sent.

## US-003 — Apply Voucher
As a Customer, I want to apply a voucher, so that I receive the correct discount before payment.
- **Given** Booking Total = 100 USD, Voucher = 10%
- **When** Voucher is applied
- **Then** Final Amount = 90 USD (Subject to `maxDiscount` limits). Voucher usage is only deducted after the payment succeeds.

## US-004 — Refund Request
As the System, I want to refund eligible cancelled bookings automatically, so customers receive their money according to company policy.
- **Given** Booking Status = CONFIRMED, Payment Status = SUCCESS
- **When** Booking is cancelled
- **Then** Refund request is sent, Payment Status = REFUND_PENDING, System will automatically retry up to 3 times.

## US-005 — Webhook Security & Idempotency
As the System, I want duplicate callbacks to be processed exactly once and malicious callbacks to be rejected.
- **Given** Invalid webhook signature OR Replay attack (>5 mins old) -> **Then** Rejected (HTTP 401), Security log created.
- **Given** Duplicate webhook for same transaction -> **Then** Ignored, preserve existing payment info, HTTP 200 OK returned.

---

# 4. Acceptance Criteria (EARS — Exhaustive)

### FR-001: Webhook Security
**WHEN** a payment webhook is received, **THE SYSTEM SHALL** verify the HMAC SHA-256 signature and timestamp before processing.
**WHERE** the webhook signature is invalid or timestamp > 5 mins old, **THE SYSTEM SHALL** reject the request, return HTTP 401 Unauthorized, and create a security audit log.

### FR-002: Idempotency & Database Locking
**WHEN** multiple webhook requests arrive simultaneously, **THE SYSTEM SHALL** ensure idempotent processing using Transaction ID and Pessimistic Write database transaction locking.
**WHERE** a duplicate webhook is received, **THE SYSTEM SHALL** ignore duplicated business processing, return HTTP 200 OK, and preserve existing payment information.

### FR-003: Payment Success Handling
**WHEN** payment verification succeeds, **THE SYSTEM SHALL**:
- update Payment Status to `SUCCESS`
- update Booking Status to `CONFIRMED`
- store Gateway Transaction ID
- increment the `currentUsage` of the applied Voucher (if any)
- create `PaymentAuditLog`

### FR-004: Voucher Validation
**WHEN** a voucher code is submitted, **THE SYSTEM SHALL** validate: voucher exists, active, not expired, minimum booking value, remaining usage, and maximum discount amount (`maxDiscount`) before calculating the payable amount.

### FR-005: Refund Processing
**WHEN** a confirmed booking is cancelled AND refund policy is satisfied, **THE SYSTEM SHALL** initiate a refund request (`REFUND_PENDING`).
**WHERE** refund fails after 3 retries, **THE SYSTEM SHALL** set status to `MANUAL_REFUND_REQUIRED` and generate an Admin notification.

---

# 5. Business Rules
- **BR-001:** One booking may have only one successful payment.
- **BR-002:** One booking may apply only one voucher.
- **BR-003:** Discount amount shall never exceed booking total or `maxDiscount`.
- **BR-004:** Refund amount shall never exceed the original payment amount.
- **BR-005:** Only `SUCCESS` payments may be refunded. A refunded payment cannot be refunded again.
- **BR-006:** Gateway Transaction ID must be unique.
- **BR-007:** Voucher usage is deducted ONLY AFTER successful payment. Failed or expired payments shall NOT consume voucher usage.
- **BR-008:** Forbidden State Transitions: `FAILED` -> `SUCCESS`, `REFUNDED` -> `SUCCESS`, `REFUNDED` -> `FAILED`.

---

# 6. API Contracts (Key Endpoints)

### Create Payment
```http
POST /api/payments
```
- **Request:** `{ "bookingId": "uuid", "paymentMethod": "STRIPE/CASH/BANK_TRANSFER" }`
- **Response 201:** `{ "transactionId": "string", "clientSecret": "string" }`

### Payment Webhook Callback
```http
POST /api/payments/webhook
```
- **Headers:** `Stripe-Signature`
- **Response 200:** Webhook processed or Idempotent duplicate handled.
- **Response 401:** Invalid Signature or Replay attack.

### Manual Confirmation Endpoints
```http
POST /api/payments/{paymentId}/confirm-cash
POST /api/payments/{paymentId}/confirm-bank
```

---

# 7. Data Models & DB Schema

## Entity: Payment
| Field | Type | Description |
|---|---|---|
| payment_id | BIGINT | Primary Key |
| booking_id | BIGINT | Foreign Key |
| transaction_id | VARCHAR | Unique ID from Gateway |
| amount | DECIMAL(18,2) | Final payable amount |
| payment_method | VARCHAR | STRIPE / CASH / BANK_TRANSFER |
| status | VARCHAR | PENDING / SUCCESS / FAILED / REFUND_PENDING / REFUNDED / MANUAL_REFUND_REQUIRED |
| refund_amount | DECIMAL(18,2) | Tracked refund amount |
| refund_retry_count | INT | Max 3 |

## Entity: Voucher
| Field | Type | Description |
|---|---|---|
| voucher_id | BIGINT | Primary Key |
| code | VARCHAR | Unique voucher code |
| discount_type | VARCHAR | PERCENTAGE / FIXED_AMOUNT |
| discount_value | DECIMAL(18,2) | Discount value |
| max_discount | DECIMAL(18,2) | Max cap for percentage discounts |
| current_usage | INT | Incremented ONLY on payment SUCCESS |
| max_usage | INT | Max allowed redemptions |

## Entity: PaymentAuditLog
| Field | Type | Description |
|---|---|---|
| audit_id | BIGINT | Primary Key |
| transaction_id | VARCHAR | Target transaction |
| action | VARCHAR | Event type (PAYMENT_SUCCESS, REFUND_FAILED, etc) |
| request_payload| TEXT | Immutable snapshot |

---

# 8. Error Handling Matrix

| Error Condition | HTTP Status | Mitigation / Retry |
|---|---|---|
| Invalid Stripe Signature | 401 Unauthorized | Reject immediately. Log security event. |
| Replay Attack (>5 mins) | 401 Unauthorized | Reject immediately. Log security event. |
| Duplicate Webhook | 200 OK | Process idempotently. Do not create duplicates. |
| Expired / Exhausted Voucher | 400 Bad Request | Return BusinessException to user. |
| Database Lock Failure | 500 Internal Error | Spring transactional rollback. |
| Gateway Refund Failure | Background Job | Retry up to 3 times via cron scheduler. |

---

# 9. Non-Functional Requirements

## Security & Reliability
- HTTPS/TLS required for all communication.
- Sensitive payment information (e.g., Credit Card numbers) must never be logged.
- Database operations (Booking, Payment, Voucher) must be ACID transactions.
- Webhooks must utilize Pessimistic Write locks to avoid race conditions.

## Performance
- Payment intent creation ≤ 5 seconds.
- Webhook verification & processing ≤ 2 seconds.

---

# 10. Rollout Plan & Dependencies
- **Dependencies:** Booking Module, Authentication Module, Notification Module, Stripe Gateway SDK.
- **Rollout Phase 1:** Deploy payment APIs & webhook handling with Stripe Test Mode keys.
- **Rollout Phase 2:** Run E2E tests for automated refund retry cron jobs.
- **Rollout Phase 3:** Switch to Production Stripe keys and monitor `PaymentAuditLog` for `WEBHOOK_VERIFICATION_FAILED` events.
