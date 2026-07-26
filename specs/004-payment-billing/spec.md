# Feature Specification: 004-payment-billing

**Feature Branch:** `004-payment-billing`  
**Created:** 2026-06-23  
**Updated:** 2026-07-23  
**Status:** APPROVED  
**Priority:** HIGH (Financial & Security)  
**Specification Level:** Formal Specification

---

# 1. Business Context & Goals

## Business Context
Customers need a secure, reliable and auditable online payment process to complete hotel bookings. The system must automatically verify payment results received from the payment gateway before updating booking status while protecting against fraudulent or replayed webhook requests. Because payment operations directly affect revenue, customer trust, and legal compliance, this feature is classified as a high-risk core module.

With the platform expanding into a Multi-Tenant Marketplace, we also support group bookings which allow deposit payments (30% - 50%), e-Invoice VAT details, combo discount vouchers, automated cancellations for unpaid expired bookings, dynamic refund rates based on time brackets, refunds for unused meals/services at check-out, and monthly payout reconciliations for hotel partners.

## Goals
The system shall:
- Allow secure online payments via Stripe and VNPAY.
- Support Deposit payments (30% to 50% of the total amount) with a countdown timer (default 10 minutes) for group booking reservations.
- Support voucher discounts, validations, and combo vouchers (e.g. Free Breakfast, dinner discount).
- Support automated refunds via a Refund Engine based on cancellation time brackets:
  - Cancellation > 7 days prior to check-in: 100% refund.
  - Cancellation 3 - 7 days prior to check-in: 50% refund.
  - Cancellation < 3 days prior to check-in: 0% refund.
- Support refunding unused meal tickets and services at check-out.
- Support capturing corporate VAT Invoice details and generating a PDF invoice download.
- Support calculating commission rates (10% - 15%) and generating monthly payouts for partners.
- Guarantee data consistency across all services using ACID transactions.

## Success Metrics
- 100% webhook/IPN requests are authenticated via HMAC SHA-256 (Stripe) and SHA-512 (VNPAY).
- Duplicate webhook/IPN processing creates 0 duplicate transactions (Idempotency).
- Payment confirmation completes within 5 seconds.
- 100% payment events generate immutable audit logs.
- 0 successful Replay Attack requests.

---

# 2. Stakeholders & User Personas

## Customer
A customer books hotel rooms and completes payment through supported payment methods.
**Goals:** Complete payment (full or deposit), apply discount/combo vouchers, receive immediate booking confirmation, request refund for cancelled bookings or unused meals, view transaction history, download VAT invoices.

## Administrator & Receptionist
Administrators manage payment records, approve payouts for partner hotels, and review failed transactions. Receptionists can handle manual/offline payments (Cash/Bank Transfer) and trigger partial refunds for unused meal tickets at check-out.

## Hotel Partner
**Goals:** Receive monthly payouts based on completed bookings minus platform commissions (10% - 15%), trace financial summaries.

## Payment Gateway (Stripe & VNPAY)
Third-party payment providers that process transactions.
**Responsibilities:** Process payments, send payment webhook/IPN callbacks, provide transaction status.

---

# 3. User Scenarios (All Paths)

## US-001 — Successful Online Payment (Full / Deposit)
As a Customer, I want to pay online via Stripe or VNPAY (either full amount or deposit of 30-50%), so that my booking is confirmed.
- **Given** Booking Status = PENDING
- **When** Valid Stripe/VNPAY callback or IPN is received
- **Then** Payment Status = SUCCESS, Booking Payment Status = SUCCESS (if full) or PARTIALLY_PAID (if deposit), Transaction ID is stored, PaymentAuditLog is created.

## US-002 — Countdown Hold Reservation
As the System, I want to cancel reservations whose payments are not completed within the 10-minute hold limit.
- **Given** Payment is created with `countdown_end_time` (now + 10 mins) and Status = PENDING
- **When** `countdown_end_time` is reached and payment is not verified
- **Then** Payment Status = FAILED, Booking Status = FAILED/CANCELLED.

## US-003 — Apply Combo Voucher
As a Customer, I want to apply a combo voucher, so that I receive discounts and food/beverage benefits (e.g. Free Breakfast).
- **Given** Booking Total = 100 USD, Voucher = "FREE_BREAKFAST" combo voucher
- **When** Voucher is applied and payment succeeds
- **Then** Booking final amount is updated, and combo benefit is active for the booking.

## US-004 — Admin Approval Refund Engine (Time Brackets)
As a Customer/Admin, I want the system to calculate the refund amount upon cancellation and require Admin approval before executing the gateway transaction.
- **Given** Booking Status = CONFIRMED, Payment Status = SUCCESS
- **When** Booking is cancelled:
  - System changes Booking Status = CANCELLED and Payment Status = REFUND_PENDING.
  - Background scheduler bypasses processing this refund since `refund_amount` is not yet set.
- **When** Admin reviews and clicks "Approve Refund" on the Admin Dashboard:
  - System calculates refund based on check-in date delta:
    - If cancellation is > 7 days prior to check-in: refund amount = 100% of payment amount.
    - If cancellation is 3 - 7 days prior to check-in: refund amount = 50% of payment amount.
    - If cancellation is < 3 days prior to check-in: refund amount = 0%.
  - System triggers gateway refund transaction (Stripe/VNPAY) and updates Payment Status = REFUNDED.
- **Then** Refund is successfully processed and recorded.

## US-005 — Refund Unused Meals at Check-out
As a Receptionist, I want to refund the customer for unused meals/services at check-out.
- **Given** Booking Status = COMPLETED/CHECKOUT, Meal tickets are unused
- **When** Staff calls refund for unused meals with amount $X
- **Then** System refunds $X to customer gateway/wallet, logs transaction, updates `meal_refund_amount` on Payment.

## US-006 — Corporate VAT Invoice Export
As a Customer, I want to input corporate billing details (Tax ID, Company Name, etc.) and download a VAT Invoice in PDF format.
- **Given** Successful payment
- **When** Customer requests e-Invoice
- **Then** PDF document is generated containing Company Name, Tax ID, Address, total amount, taxes, and service fees.

## US-007 — Partner Payout & Reconciliation
As the System, I want to calculate monthly revenue for hotels, deduct platform commission (10% - 15%), and allow Admins to approve payouts.
- **Given** End of billing month
- **When** Admin triggers calculation
- **Then** Payout records are created. Upon approval, status is set to PAID.

---

# 4. Acceptance Criteria (EARS — Exhaustive)

### FR-001: Webhook & IPN Security
**WHEN** a payment webhook/callback is received, **THE SYSTEM SHALL** verify signature and timestamp before processing.
**WHERE** signature is invalid, **THE SYSTEM SHALL** reject the request, return HTTP 401, and write an audit log.

### FR-002: Hold Reservation Cancellation
**WHEN** the check task runs, **THE SYSTEM SHALL** query all payments with Status = PENDING and countdown_end_time < NOW, set their status to FAILED, and update their respective Bookings to FAILED/CANCELLED.

### FR-003: Time-Bracket Refund Calculation & Approval
**WHEN** an Admin approves a refund request via `/api/v1/payments/{bookingId}/refund`, **THE SYSTEM SHALL** calculate the refund amount based on the check-in date:
- `CancelDate < CheckInDate - 7 days`: Refund 100%
- `CancelDate` between `CheckInDate - 7 days` and `CheckInDate - 3 days`: Refund 50%
- `CancelDate > CheckInDate - 3 days`: Refund 0%
**AND THE SYSTEM SHALL** initiate the API refund with Stripe or simulate for VNPAY, updating the payment status to `REFUNDED`.

### FR-004: Corporate VAT Invoice Generation
**WHEN** a request is made for `/api/v1/payments/{paymentId}/invoice`, **THE SYSTEM SHALL** compile the billing data (Tax ID, Company Name, Address, base amount, service fee, taxes, total) and return a PDF file byte stream.

### FR-005: Payout Settlement
**WHEN** a monthly payout is approved by an administrator, **THE SYSTEM SHALL** update the payout status to `PAID` and write a payout audit log.

---

# 5. Business Rules
- **BR-001:** Deposit payments must have `is_deposit` set to true, and specify `deposit_ratio` between 0.30 and 0.50.
- **BR-002:** Unpaid pending bookings expire 10 minutes after intent creation.
- **BR-003:** Refund amount for cancellations shall be determined by check-in date delta.
- **BR-004:** Refund for unused meals at check-out must not exceed booking total cost.
- **BR-005:** Monthly payout amount calculation: $\text{Payout Amount} = \text{Total Completed Booking Revenue} \times (1 - \text{Commission Rate})$.

---

# 6. API Contracts (Key Endpoints)

### Create Payment
```http
POST /api/v1/payments/create
```
- **Request:**
  ```json
  {
    "bookingId": 1,
    "paymentMethod": "STRIPE/VNPAY/CASH/BANK_TRANSFER",
    "isDeposit": true,
    "depositRatio": 0.30,
    "companyName": "ABC Corp",
    "taxId": "0102030405",
    "companyAddress": "Hanoi, Vietnam",
    "companyEmail": "billing@abc.com"
  }
  ```
- **Response 200:**
  ```json
  {
    "transactionId": "txn_123",
    "clientSecret": "stripe_sec_or_vnpay_url",
    "countdownEndTime": "2026-07-23T18:50:00Z"
  }
  ```

### VNPAY IPN Callback
```http
POST /api/v1/payments/vnpay-ipn
```
- **Response 200:** `{ "RspCode": "00", "Message": "Confirm Success" }`

### Refund Unused Meal Tickets
```http
POST /api/v1/payments/{bookingId}/refund-meals
```
- **Request:** `{ "unusedAmount": 15.00 }`
- **Response 200:** `{ "refundStatus": "REFUNDED", "amount": 15.00 }`

### Get e-Invoice PDF
```http
GET /api/v1/payments/{paymentId}/invoice
```
- **Response 200:** File stream (Application/pdf)

### Payout Endpoints
```http
POST /api/v1/payments/payout/calculate?hotelId=1&startDate=2026-07-01T00:00:00&endDate=2026-07-31T23:59:59
POST /api/v1/payments/payout/{payoutId}/approve
GET /api/v1/payments/payout/hotel/{hotelId}
```

---

# 7. Data Models & DB Schema

## Entity: Payment (Updated)
- `is_deposit` (BIT/BOOLEAN)
- `deposit_ratio` (DECIMAL)
- `countdown_end_time` (TIMESTAMP)
- `meal_refund_amount` (DECIMAL)
- `invoice_company_name` (NVARCHAR)
- `invoice_tax_id` (VARCHAR)
- `invoice_company_address` (NVARCHAR)
- `invoice_company_email` (VARCHAR)

## Entity: Payout
- `payout_id` (BIGINT PK)
- `hotel_id` (BIGINT)
- `period_start` (TIMESTAMP)
- `period_end` (TIMESTAMP)
- `total_revenue` (DECIMAL)
- `commission_rate` (DECIMAL)
- `payout_amount` (DECIMAL)
- `status` (VARCHAR)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

---

# 8. Error Handling Matrix
- **Expired Hold:** Returns booking cancelled status.
- **Invalid Checksum:** VNPAY returns RspCode `97` (Invalid Signature).
- **Over-Refund Limit:** Returns 400 Bad Request if total refund amount exceeds booking total.
