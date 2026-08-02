# Implementation Plan: 009-refund-policy-engine

**Branch**: `009-refund-policy-engine` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Introduce a configurable, rule-based refund policy engine that calculates refund amounts based on cancellation timing relative to check-in. Fixes the existing NPE in `PaymentServiceImpl.retryFailedRefunds()` (null `refundAmount`). Provides admin-configurable policies via CRUD API and a full refund audit trail.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V22__Refund_policy_engine.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.payment.refund` — new package for refund policy
- **Existing Dependencies**: Extends existing `Payment`, `Booking`, `PaymentRepository`; reuses `BookingService` cancellation flow
- **DTOs**: `RefundPreviewResponse`, `RefundPolicyRequest`, `RefundPolicyResponse`, `RefundAuditLogResponse`, `CancelBookingRequest`
- **No business logic in controllers**: All policy evaluation and refund calculation in `RefundPolicyServiceImpl`

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V22__Refund_policy_engine.sql`
  - Create `refund_policies` table with all rule fields
  - Create `refund_audit_logs` table
  - Seed default policy rules (100%/7days, 50%/3-6days, 0%/0-2days)
  - Index on `refund_policies.days_before_checkin` and `refund_audit_logs.booking_id`

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/payment/refund/RefundPolicy.java`
- `src/main/java/com/hotelbooking/payment/refund/RefundPolicyRepository.java`
- `src/main/java/com/hotelbooking/payment/refund/RefundAuditLog.java`
- `src/main/java/com/hotelbooking/payment/refund/RefundAuditLogRepository.java`

#### Services
- `src/main/java/com/hotelbooking/payment/refund/RefundPolicyService.java` — Interface
- `src/main/java/com/hotelbooking/payment/refund/RefundPolicyServiceImpl.java` — Policy CRUD, policy evaluation (find matching rule by days remaining), refund calculation with null-safety
- `src/main/java/com/hotelbooking/payment/PaymentServiceImpl.java` — Fix `retryFailedRefunds()`: null-check `refundAmount`, use `amount` as fallback; integrate refund policy in cancellation flow
- `src/main/java/com/hotelbooking/booking/BookingServiceImpl.java` — Integrate refund preview and policy-driven cancellation

#### DTOs
- `src/main/java/com/hotelbooking/payment/refund/dto/RefundPreviewResponse.java`
- `src/main/java/com/hotelbooking/payment/refund/dto/RefundPolicyRequest.java`
- `src/main/java/com/hotelbooking/payment/refund/dto/RefundPolicyResponse.java`
- `src/main/java/com/hotelbooking/payment/refund/dto/RefundAuditLogResponse.java`
- `src/main/java/com/hotelbooking/booking/dto/CancelBookingRequest.java` — Add optional `refundOverride` field

#### Controllers
- `src/main/java/com/hotelbooking/payment/refund/RefundPolicyController.java` — Admin CRUD for refund policies
- Extend `src/main/java/com/hotelbooking/booking/BookingController.java` — Add refund preview and cancellation with override
- `src/main/java/com/hotelbooking/payment/AdminRefundController.java` — Refund audit logs endpoint

### Testing
- `src/test/java/com/hotelbooking/payment/refund/RefundPolicyServiceTest.java` — Unit tests: policy evaluation, null-safety fix, refund calculation, override, audit log
- `src/test/java/com/hotelbooking/payment/PaymentServiceTest.java` — Fix existing `testRetryFailedRefunds` by setting `refundAmount` on mock

## Non-Functional Requirements
- **Bug Fix**: `retryFailedRefunds()` MUST handle null `refundAmount` — this is the primary P0 fix from the previous session.
- **Security**: Refund policy admin endpoints restricted to ADMIN. Audit logs are read-only.
- **Audit**: Every refund calculation is logged in `refund_audit_logs`. No exceptions silently swallow audit records.
- **Performance**: Policy evaluation is O(n) scan of active policies sorted by `days_before_checkin DESC` (small table, < 20 policies expected).
