# Feature Specification: 010-voucher-store-front

**Feature Branch**: `010-voucher-store-front`
**Created**: 2026-07-23
**Status**: Approved & Completed
**Primary Actor(s)**: Customer, CorporateMember, Admin
**Related Use Cases**: SCR-109, SCR-110, UC-15

---

## 1. Context & Goal

The existing voucher system (from spec 004-payment-billing) handles voucher validation during payment but does not provide a customer-facing voucher store or admin management interface. This module introduces a voucher store where customers can browse, filter, and "claim" vouchers to their wallet, and an admin panel where administrators can create, update, deactivate, and monitor voucher campaigns.

**Goal**: Provide a customer-facing voucher marketplace. Enable admins to manage voucher campaigns (create, schedule, deactivate). Track voucher claim and usage statistics per voucher and per customer.

---

## 2. Actors & Roles

**Customer / CorporateMember**: Browses available vouchers, claims vouchers to personal wallet, views own voucher balance and usage history.
**Admin**: Creates and manages voucher campaigns, sets discount rules, schedules voucher availability, views usage reports.
**System**: Automatically validates and applies vouchers at checkout (existing behavior from spec 004).

---

## 3. Functional Requirements

### FR-001: Admin Voucher CRUD
THE system SHALL allow Admins to create vouchers via `POST /api/v1/admin/vouchers`.
THE system SHALL allow Admins to update voucher details via `PUT /api/v1/admin/vouchers/{voucherId}`.
THE system SHALL allow Admins to deactivate vouchers via `DELETE /api/v1/admin/vouchers/{voucherId}` (soft delete — sets `is_active = false`).
THE system SHALL allow Admins to list all vouchers with pagination and filters.

### FR-002: Voucher Fields
EACH voucher SHALL have:
- `code` (unique, auto-generated or admin-defined)
- `name` (campaign name)
- `description`
- `discountType` (`PERCENTAGE` or `FIXED_AMOUNT`)
- `discountValue` (e.g., 10 for 10% or 10 USD)
- `maxDiscount` (cap for percentage discounts)
- `minBookingValue` (minimum booking amount to use voucher)
- `currentUsage` / `maxUsage` (per-voucher redemption limit)
- `startDate` / `endDate` (availability window)
- `isActive` (soft enable/disable)
- `forAccountType` (`ALL`, `CUSTOMER`, `CORPORATE_MEMBER`) — restrict voucher to certain account types
- `createdBy` (admin user who created it)
- `createdAt`

### FR-003: Customer Voucher Store
THE system SHALL expose `GET /api/v1/vouchers/available` — lists all currently active and valid vouchers the customer can claim.
THE system SHALL filter by `forAccountType` — customers only see `ALL` or `CUSTOMER` vouchers; corporate members see `ALL` or `CORPORATE_MEMBER`.
THE system SHALL filter by `startDate <= now <= endDate`.
THE system SHALL filter by `currentUsage < maxUsage`.

### FR-004: Claim Voucher
WHEN a Customer claims a voucher via `POST /api/v1/users/me/vouchers/claim/{voucherCode}`, THE system SHALL:
1. Validate the voucher is active, within date range, and has remaining usage
2. Check `forAccountType` matches the user's account type
3. Check the user has not already claimed this voucher (one claim per user per voucher)
4. Create a `UserVoucher` record linking user to voucher
5. Return confirmation with voucher details

### FR-005: My Vouchers Wallet
THE system SHALL expose `GET /api/v1/users/me/vouchers` — lists all vouchers the user has claimed.
EACH `UserVoucher` record SHALL have: `voucherCode`, `name`, `discountType`, `discountValue`, `maxDiscount`, `minBookingValue`, `endDate`, `isUsed` (boolean).

### FR-006: Voucher Auto-Application at Checkout
WHEN a customer applies a voucher code at checkout (existing spec 004), THE system SHALL first check if the user has the voucher claimed in their wallet.
WHERE the voucher is not in the user's wallet, THE system SHALL reject with error `VOUCHER_NOT_CLAIMED`.

### FR-007: Voucher Usage Tracking
WHEN a booking with a voucher is confirmed (payment SUCCESS), THE system SHALL:
1. Increment `currentUsage` on the Voucher
2. Set `isUsed = true` on the user's `UserVoucher` record
3. Record the `bookingId` on the `UserVoucher`

### FR-008: Admin Voucher Statistics
THE system SHALL allow Admins to view per-voucher statistics: total claims, total redemptions, total discount given.
`GET /api/v1/admin/vouchers/{voucherId}/stats`
- Returns: `{ "voucherId", "code", "name", "totalClaims", "totalRedemptions", "totalDiscountGiven", "remainingUsage" }`

---

## 4. Data Model

### Extended Voucher Entity (existing fields from spec 004)
New fields added to existing `voucher` table:
| Field | Type | Description |
|---|---|---|
| name | VARCHAR | Campaign name |
| description | TEXT | Campaign description |
| start_date | DATE | When voucher becomes claimable |
| end_date | DATE | When voucher expires |
| for_account_type | VARCHAR | `ALL`, `CUSTOMER`, `CORPORATE_MEMBER` |
| created_by | BIGINT | FK to users (admin) |

### UserVoucher (new)
| Field | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| user_id | BIGINT | FK to users |
| voucher_id | BIGINT | FK to vouchers |
| claimed_at | TIMESTAMP | When claimed |
| is_used | BOOLEAN | Whether already redeemed |
| booking_id | BIGINT | FK to bookings (nullable) |
| used_at | TIMESTAMP | When redeemed |

---

## 5. API Contracts

### Customer: Browse Available Vouchers
```
GET /api/v1/vouchers/available?page=0&size=20
```
- **Auth**: Bearer token
- **Response 200**: Paginated list of claimable vouchers (filtered by account type, date, usage)

### Customer: Claim Voucher
```
POST /api/v1/users/me/vouchers/claim/{voucherCode}
```
- **Auth**: Bearer token
- **Response 200**: `{ "message": "Voucher claimed successfully", "voucher": {...} }`
- **Response 400**: `VOUCHER_NOT_AVAILABLE`, `VOUCHER_ALREADY_CLAIMED`, `VOUCHER_NOT_FOR_YOUR_ACCOUNT_TYPE`

### Customer: My Voucher Wallet
```
GET /api/v1/users/me/vouchers?page=0&size=20
```
- **Auth**: Bearer token
- **Response 200**: Paginated list of claimed UserVouchers with voucher details

### Admin: Create Voucher
```
POST /api/v1/admin/vouchers
Content-Type: application/json
```
- **Request**: `{ "code", "name", "description", "discountType", "discountValue", "maxDiscount", "minBookingValue", "maxUsage", "startDate", "endDate", "forAccountType" }`
- **Response 201**: `{ "voucherId", "code", "message": "Voucher created successfully" }`

### Admin: Update Voucher
```
PUT /api/v1/admin/vouchers/{voucherId}
Content-Type: application/json
```
- **Auth**: Bearer token (ADMIN)
- **Request**: Same fields as create (partial update supported)
- **Response 200**: Updated voucher

### Admin: Deactivate Voucher
```
DELETE /api/v1/admin/vouchers/{voucherId}
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: `{ "message": "Voucher deactivated successfully" }`
- Deactivation does NOT affect vouchers already claimed or used

### Admin: List All Vouchers
```
GET /api/v1/admin/vouchers?status=ACTIVE&accountType=ALL&page=0&size=20
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: Paginated voucher list with current usage stats

### Admin: Voucher Statistics
```
GET /api/v1/admin/vouchers/{voucherId}/stats
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: `{ "voucherId", "code", "totalClaims", "totalRedemptions", "totalDiscountGiven" }`

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Voucher code not found | 404 Not Found | VOUCHER_NOT_FOUND |
| Voucher expired or not yet active | 400 Bad Request | VOUCHER_NOT_AVAILABLE |
| Voucher fully redeemed | 400 Bad Request | VOUCHER_EXHAUSTED |
| Account type not eligible | 400 Bad Request | VOUCHER_NOT_FOR_YOUR_ACCOUNT_TYPE |
| Voucher already claimed | 400 Bad Request | VOUCHER_ALREADY_CLAIMED |
| Voucher deactivated | 400 Bad Request | VOUCHER_NOT_AVAILABLE |
| Non-admin creates voucher | 403 Forbidden | ACCESS_DENIED |
| Booking below minBookingValue | 400 Bad Request | MINIMUM_BOOKING_VALUE_NOT_MET |
| Voucher not in user's wallet at checkout | 400 Bad Request | VOUCHER_NOT_CLAIMED |

---

## 7. User Scenarios & Testing

### US-1: Browse Available Vouchers (Priority: P1)
As a Customer, I want to see all vouchers I can claim, so I can add discounts to my next booking.

**Given** I am authenticated as a CUSTOMER
**When** calling `GET /api/v1/vouchers/available`
**Then** I receive only vouchers where `forAccountType = ALL or CUSTOMER`, `isActive = true`, `currentUsage < maxUsage`, and current date is within `[startDate, endDate]`

### US-2: Claim a Voucher (Priority: P1)
As a CorporateMember, I want to claim a corporate-exclusive voucher, so I can use it for my next booking.

**Given** a voucher with `forAccountType = CORPORATE_MEMBER` is active
**When** I claim it via `POST /api/v1/users/me/vouchers/claim/{code}`
**Then** a `UserVoucher` record is created, and I can see it in my wallet

### US-3: Cannot Claim Same Voucher Twice (Priority: P1)
As a Customer, I want the system to prevent double-claiming, so vouchers remain fair.

**Given** I have already claimed a voucher with code `SUMMER20`
**When** I try to claim `SUMMER20` again
**Then** I receive HTTP 400 with message `VOUCHER_ALREADY_CLAIMED`

### US-4: Admin Creates Voucher Campaign (Priority: P1)
As an Admin, I want to create a new voucher campaign, so I can run promotions.

**Given** I am authenticated as an ADMIN
**When** I create a voucher with code `SUMMER25`, 25% off, maxUsage=100, forAccountType=ALL
**Then** the voucher is created with `currentUsage=0`, `isActive=true`, and visible in the store

### US-5: Voucher Usage at Checkout (Priority: P1)
As a Customer, I want to apply my claimed voucher at checkout, so I get the discount.

**Given** I have claimed `SUMMER20` to my wallet
**When** I apply `SUMMER20` at checkout
**Then** the discount is calculated and applied (existing spec 004 validation still applies)
**And** `currentUsage` increments, `isUsed=true` on my `UserVoucher`

### US-6: Admin Views Voucher Stats (Priority: P2)
As an Admin, I want to see how many times a voucher was claimed and used, so I can measure campaign success.

**Given** a voucher `SUMMER20` with 50 total claims and 30 redemptions
**When** Admin calls `GET /api/v1/admin/vouchers/{id}/stats`
**Then** response shows `totalClaims=50`, `totalRedemptions=30`, `remainingUsage=70`

### US-7: Admin Deactivates Voucher (Priority: P2)
As an Admin, I want to deactivate a voucher mid-campaign, so I can stop new claims.

**Given** voucher `FLASHSALE` is active with 80 claims
**When** Admin calls `DELETE /api/v1/admin/vouchers/{id}`
**Then** `isActive=false`, existing claims remain, new claims rejected

---

## 8. Acceptance Criteria

AC-029: Admins can create, update, deactivate, and list vouchers with all required fields.
AC-030: Customers see only eligible vouchers (account type, date range, availability) in the store.
AC-031: Each user can claim a voucher only once.
AC-032: Claimed vouchers appear in the user's wallet.
AC-033: Only claimed vouchers can be applied at checkout.
AC-034: `currentUsage` increments and `UserVoucher.isUsed` is set when a booking using a voucher is confirmed.
AC-035: Admin can view per-voucher statistics (claims, redemptions, discount given).
AC-036: Deactivated vouchers cannot be claimed by new users.

---

## 9. Out of Scope

- Voucher code redemption without booking (no cash-out)
- Voucher transfer between users
- Stacking multiple vouchers on one booking
- Auto-suggestion of best voucher at checkout (Phase 2)
- Email notifications when voucher is about to expire (Phase 2)
- Referral voucher system (creating vouchers by referring friends)
- Voucher categories/tags for filtering
