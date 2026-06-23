# Feature Specification: 004-payment-billing

**Feature Branch**: `004-payment-billing`
**Created**: 2026-06-23
**Status**: Completed

## User Scenarios & Testing

### User Story 1 - Online Payment & Webhook Callback (Priority: P1)
As a Customer, I want to pay for my pending booking online so my booking can be confirmed.

**Why this priority**: Crucial for completing the booking lifecycle and securing revenue.
**Independent Test**: Simulate gateway webhook call with valid signature to confirm booking.

**Acceptance Scenarios**:
1. **Given** a PENDING booking, **When** gateway triggers webhook with signature matching, **Then** payment status becomes `SUCCESS` and booking is marked `CONFIRMED`.
2. **Given** a callback request, **When** signature is invalid, **Then** security exception is thrown and payment is rejected.

### User Story 2 - Voucher Discount Application (Priority: P2)
As a Customer, I want to apply a discount voucher to my booking, so I can save money on my stay.

**Why this priority**: Drives sales and customer retention.
**Independent Test**: Apply a valid voucher to a booking request and verify total price is correctly recalculated.

**Acceptance Scenarios**:
1. **Given** a valid percent-based voucher (e.g. 10%), **When** applied to a booking of $100, **Then** total price becomes $90.
2. **Given** an expired voucher, **When** applied, **Then** a business exception is thrown.

### User Story 3 - Booking Refund (Priority: P2)
As the System, I want to process refunds automatically when a booking is cancelled, to comply with company policies.

**Why this priority**: Required for customer trust and legal compliance.
**Independent Test**: Cancel a confirmed booking and verify refund transaction is initiated.

**Acceptance Scenarios**:
1. **Given** a cancelled booking, **When** eligible for refund, **Then** refund transaction is created and gateway is called.

## Requirements

### Functional Requirements
- **FR-001**: System must verify HMAC signatures on gateway webhook callbacks to prevent payment spoofing.
- **FR-002**: Voucher codes must be validated: check active dates, usage limits, and minimum booking value.
- **FR-003**: System must handle discount limits: max discount amount for percentage vouchers.
- **FR-004**: Payment operations must be audited in the `PaymentAuditLog` table.

### Key Entities
- **Payment**: Represents a transaction. Fields: `paymentId`, `bookingId`, `amount`, `status` (PENDING, SUCCESS, FAILED, REFUNDED), `transactionId`.
- **Voucher**: Represents discount codes. Fields: `voucherId`, `code`, `discountType` (PERCENTAGE, FIXED), `discountValue`, `maxUsage`, `minBookingValue`, `expiryDate`.
- **PaymentAuditLog**: Stores security and debug details for transaction webhooks.

## Success Criteria
- **SC-001**: 100% of payments verified via webhook HMAC signature.
- **SC-002**: Vouchers validate usage limits and expire automatically.
