# Tasks: 004-payment-billing

**Input**: Design documents from `/specs/004-payment-billing/`

## Phase 1: Voucher System
- [x] T001 Define `Voucher` JPA entity and `VoucherRepository`
- [x] T002 Implement voucher creation and filtering logic in `VoucherServiceImpl`
- [x] T003 Expose `/api/v1/vouchers/apply` in `VoucherController`
- [x] T004 Write unit tests in `VoucherServiceTest` checking percent/fixed calculations and usage validations

## Phase 2: Payment Webhooks & Processing
- [x] T005 Define `Payment` and `PaymentAuditLog` JPA entities
- [x] T006 Implement HMAC-SHA256 signature verification utility in the security package
- [x] T007 Implement callback parsing and database state transitions in `PaymentServiceImpl`
- [x] T008 Expose webhook callback endpoint `/api/v1/payments/webhook` in `PaymentController`
- [x] T009 Write unit tests in `PaymentServiceTest` simulating signature match, mismatch, and double payment edge cases
