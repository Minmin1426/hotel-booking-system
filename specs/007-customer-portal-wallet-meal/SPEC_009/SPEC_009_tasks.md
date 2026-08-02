# Tasks: 009-refund-policy-engine

**Input**: Design documents from `/specs/009-refund-policy-engine/`

## Phase 1: Database & Entities
- [ ] T001 Create Flyway migration `V22__Refund_policy_engine.sql` — `refund_policies` table, `refund_audit_logs` table, seed default rules, add indexes
- [ ] T002 Implement `RefundPolicy` JPA entity with all fields and `@Enumerated`
- [ ] T003 Implement `RefundAuditLog` JPA entity with all fields
- [ ] T004 Implement `RefundPolicyRepository` and `RefundAuditLogRepository`

## Phase 2: Refund Policy Service
- [ ] T005 Implement `RefundPolicyServiceImpl.getRefundPreview()` — calculate days remaining, find matching policy rule, compute refund amount
- [ ] T006 Implement null-safe refund calculation: WHERE `refundAmount == null` → use `amount`; WHERE both null → set `MANUAL_REFUND_REQUIRED`
- [ ] T007 Implement `RefundPolicyService.calculateRefund()` — policy evaluation logic: find highest-priority rule where `daysRemaining >= daysBeforeCheckin`
- [ ] T008 Implement `RefundPolicyService.createRefundAuditLog()` — record every refund calculation with all fields
- [ ] T009 Implement `RefundPolicyService.getPolicies()` — list all active/inactive policies
- [ ] T010 Implement `RefundPolicyService.createPolicy()` — admin creates new rule
- [ ] T011 Implement `RefundPolicyService.updatePolicy()` — admin updates existing rule

## Phase 3: Payment Service Fix & Integration
- [ ] T012 **FIX BUG**: In `PaymentServiceImpl.retryFailedRefunds()`, add null-check for `refundAmount` before arithmetic — use `payment.getAmount()` as fallback; if both null, set status to `MANUAL_REFUND_REQUIRED`
- [ ] T013 Update `PaymentServiceImpl.cancelBooking()` (or booking cancellation flow) to call `RefundPolicyService.calculateRefund()` and set `refundAmount`
- [ ] T014 Update `CancelBookingRequest` DTO — add optional `refundOverride` field for admin overrides
- [ ] T015 In cancellation flow: if `refundOverride` is present → use override percentage; else → use policy engine

## Phase 4: Controller Endpoints
- [ ] T016 Add `GET /api/v1/bookings/{bookingId}/refund-preview` — returns refund preview for booking owner or admin
- [ ] T017 Add `POST /api/v1/admin/bookings/{bookingId}/cancel` — admin cancellation with optional `refundOverride`
- [ ] T018 Add `POST /api/v1/admin/refund-policies` — create policy
- [ ] T019 Add `GET /api/v1/admin/refund-policies` — list policies
- [ ] T020 Add `PUT /api/v1/admin/refund-policies/{policyId}` — update policy
- [ ] T021 Add `GET /api/v1/admin/refund-audit-logs` — paginated audit log with filters

## Phase 5: Error Handling
- [ ] T022 Handle `BOOKING_NOT_CONFIRMED` → 400
- [ ] T023 Handle `BOOKING_ALREADY_CANCELLED` → 400
- [ ] T024 Handle `NO_PAYMENT_FOUND` → 400
- [ ] T025 Handle `PAYMENT_ALREADY_REFUNDED` → 400
- [ ] T026 Handle `INVALID_REFUND_PERCENTAGE` (> 100 or < 0) → 400
- [ ] T027 Add `RefundException` to `GlobalExceptionHandler`

## Phase 6: Testing
- [ ] T028 Fix existing `PaymentServiceTest.testRetryFailedRefunds()` — add `payment.setRefundAmount(BigDecimal.valueOf(500))` to the mock setup
- [ ] T029 Add new test `testRetryFailedRefunds_withNullRefundAmount()` — verify fallback to `amount` when `refundAmount` is null (the actual bug scenario)
- [ ] T030 Write `RefundPolicyServiceTest.java` — unit tests: 100%/7+ days, 50%/3-6 days, 0%/<3 days, null refund amount fallback, override takes precedence, audit log created
- [ ] T031 Verify all new acceptance criteria (AC-021 to AC-028) are covered
