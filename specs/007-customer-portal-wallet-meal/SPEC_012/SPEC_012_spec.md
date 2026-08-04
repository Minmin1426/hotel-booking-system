# Feature Specification: 012-customer-wallet

**Feature Branch**: `012-customer-wallet`
**Created**: 2026-07-23
**Status**: Approved & Completed
**Primary Actor(s)**: Customer, CorporateMember, Admin
**Related Use Cases**: SCR-105

---

## 1. Context & Goal

Customers need a digital wallet to store pre-loaded funds for fast booking checkout and to receive loyalty cashback. Corporate members have an additional "shared wallet" for the entire group with team spending limits. Wallets track balance changes over time and integrate with the booking payment flow.

**Goal**: Provide a transparent wallet system with personal and corporate variants. Support deposits, payments, refunds, and full transaction history.

---

## 2. Actors & Roles

**Customer**: Owns a personal wallet. Top up, view balance, view history, pay for bookings.
**CorporateMember**: Has BOTH a personal wallet AND a shared group wallet. Can top up both. Can view team members' balances (within the group).
**GroupOwner** (CorporateMember with OWNER flag): Manages the shared group wallet, sets individual member spending limits (links to spec 013).
**Admin**: Manages wallet freezes, manual balance corrections, and resolves disputes.

---

## 3. Functional Requirements

### FR-001: Wallet Entity
Each user SHALL have ONE `Wallet` record per `walletType`:
- `PERSONAL` (default for all users)
- `GROUP` (only for CorporateMember if they belong to a group)

### FR-002: Wallet Fields
A wallet record SHALL have: `walletId`, `userId` (owner), `walletType` (PERSONAL/GROUP), `groupId` (nullable), `balance`, `currency`, `status` (ACTIVE, FROZEN, CLOSED), `createdAt`, `updatedAt`.

### FR-003: Wallet Creation
WHEN a user is created, THE system SHALL automatically create a `PERSONAL` wallet with balance = 0 and status = ACTIVE.
WHEN a CorporateMember is added to a group, THE system SHALL NOT create a new wallet; the group has one shared wallet.

### FR-004: Group Wallet
THE system SHALL have a `Group` entity representing a corporate group. A Group has ONE shared wallet owned by the `GroupOwner`.
WHEN the GroupOwner changes, THE system SHALL transfer wallet ownership but preserve balance.

### FR-005: Deposit (Top-Up)
A user SHALL be able to deposit funds into their PERSONAL or GROUP wallet.
THE deposit SHALL be processed via the payment module (Stripe or Bank Transfer).
EACH successful deposit SHALL create a `WalletTransaction` with type `DEPOSIT`.

### FR-006: Wallet Payment
WHERE a user pays for a booking using wallet funds, THE system SHALL:
1. Validate sufficient balance
2. Deduct amount atomically (DB transaction)
3. Create `WalletTransaction` with type `PAYMENT`
4. Update corresponding `Booking.paymentStatus` to `SUCCESS`

### FR-007: Refund to Wallet
WHEN a booking paid via wallet is refunded, THE system SHALL credit the refund amount back to the source wallet (PERSONAL or GROUP).
THE system SHALL create `WalletTransaction` with type `REFUND`.

### FR-008: Transaction History
THE system SHALL record every wallet operation in `wallet_transactions` with: transactionId, walletId, type, amount, balanceBefore, balanceAfter, relatedBookingId, timestamp.

### FR-009: Wallet Status Management
A wallet in `FROZEN` status SHALL NOT allow PAYMENT or DEPOSIT operations but SHALL allow REFUND (so users can still receive money).
A wallet in `CLOSED` status SHALL NOT allow any operations.

### FR-010: Balance Check
WHERE wallet balance < required payment amount, THE system SHALL return `INSUFFICIENT_BALANCE` error (HTTP 400).

---

## 4. Data Model

### Wallet
| Field | Type | Description |
|---|---|---|
| wallet_id | BIGINT | PK |
| owner_user_id | BIGINT | FK to users (PERSONAL wallet owner OR GroupOwner) |
| wallet_type | VARCHAR | PERSONAL or GROUP |
| group_id | BIGINT | FK to groups, nullable (only for GROUP wallets) |
| balance | DECIMAL(18,2) | Current balance |
| currency | VARCHAR | Default VND |
| status | VARCHAR | ACTIVE, FROZEN, CLOSED |
| created_at | TIMESTAMP | Auto-generated |
| updated_at | TIMESTAMP | Auto-updated |

### Group
| Field | Type | Description |
|---|---|---|
| group_id | BIGINT | PK |
| group_name | VARCHAR | Group display name |
| owner_user_id | BIGINT | FK to users (CorporateMember who owns the group) |
| tax_code | VARCHAR | Optional corporate tax code |
| created_at | TIMESTAMP | Auto-generated |

### GroupMembership
| Field | Type | Description |
|---|---|---|
| membership_id | BIGINT | PK |
| group_id | BIGINT | FK to groups |
| member_user_id | BIGINT | FK to users |
| spending_limit | DECIMAL(18,2) | Per-member spending limit (links to spec 013) |
| joined_at | TIMESTAMP | Auto-generated |

### WalletTransaction
| Field | Type | Description |
|---|---|---|
| transaction_id | BIGINT | PK |
| wallet_id | BIGINT | FK to wallets |
| type | VARCHAR | DEPOSIT, PAYMENT, REFUND, ADJUSTMENT |
| amount | DECIMAL(18,2) | Signed: positive for deposit/refund, negative for payment |
| balance_before | DECIMAL(18,2) | Balance before transaction |
| balance_after | DECIMAL(18,2) | Balance after transaction |
| related_booking_id | BIGINT | FK to bookings, nullable |
| description | TEXT | Optional human-readable note |
| created_at | TIMESTAMP | Auto-generated |

---

## 5. API Contracts

### Get My Wallets
```
GET /api/v1/users/me/wallets
```
- **Auth**: Bearer token
- **Response 200**: `[{ "walletId", "walletType", "balance", "currency", "status" }, ...]`

### Get Wallet Balance
```
GET /api/v1/users/me/wallets/{walletId}/balance
```
- **Auth**: Bearer token (wallet owner only)
- **Response 200**: `{ "walletId", "balance", "currency" }`

### Get Wallet Transaction History
```
GET /api/v1/users/me/wallets/{walletId}/transactions?type=&page=0&size=20
```
- **Auth**: Bearer token (wallet owner only)
- **Response 200**: Paginated list of WalletTransaction

### Deposit to Wallet
```
POST /api/v1/users/me/wallets/{walletId}/deposit
Content-Type: application/json
```
- **Auth**: Bearer token (wallet owner only)
- **Request**: `{ "amount": 1000000, "paymentMethod": "STRIPE" | "BANK_TRANSFER" }`
- **Response 200**: `{ "transactionId", "amount", "newBalance", "paymentUrl": "..." }`

### Pay with Wallet
```
POST /api/v1/users/me/wallets/pay-booking
Content-Type: application/json
```
- **Auth**: Bearer token
- **Request**: `{ "walletId", "bookingId" }`
- **Response 200**: `{ "transactionId", "amountDeducted", "remainingBalance", "bookingStatus" }`

### Admin: List All Wallets
```
GET /api/v1/admin/wallets?userId=&page=0&size=20
```
- **Auth**: Bearer token (ADMIN)

### Admin: Freeze/Unfreeze Wallet
```
PATCH /api/v1/admin/wallets/{walletId}/status
Content-Type: application/json
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "status": "FROZEN" | "ACTIVE" | "CLOSED", "reason": "..." }`

### Admin: Manual Balance Adjustment
```
POST /api/v1/admin/wallets/{walletId}/adjust
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "amount": 100000, "type": "CREDIT" | "DEBIT", "reason": "Compensation for cancelled booking" }`

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Insufficient balance | 400 Bad Request | INSUFFICIENT_BALANCE |
| Wallet frozen | 403 Forbidden | WALLET_FROZEN |
| Wallet closed | 403 Forbidden | WALLET_CLOSED |
| Non-owner accessing wallet | 403 Forbidden | ACCESS_DENIED |
| Wallet not found | 404 Not Found | WALLET_NOT_FOUND |
| Deposit amount <= 0 | 400 Bad Request | INVALID_AMOUNT |

---

## 7. User Scenarios & Testing

### US-1: View Personal Wallet (Priority: P1)
As a Customer, I want to see my personal wallet balance, so I know how much I have.

**Given** I am authenticated with a PERSONAL wallet created at registration
**When** I call `GET /api/v1/users/me/wallets`
**Then** I see my PERSONAL wallet with current balance

### US-2: Top Up Personal Wallet (Priority: P1)
As a Customer, I want to deposit 1,000,000 VND into my wallet, so I can use it for future bookings.

**Given** My wallet has balance 0
**When** I call `POST /api/v1/users/me/wallets/{id}/deposit` with amount=1,000,000 and paymentMethod=STRIPE
**Then** I receive a Stripe payment URL, and upon payment success my wallet balance becomes 1,000,000 VND

### US-3: Pay for Booking Using Wallet (Priority: P1)
As a Customer, I want to pay for my booking using wallet funds, so I don't need a credit card.

**Given** My wallet has balance 2,000,000 VND and I have a PENDING booking with amount 1,500,000 VND
**When** I call `POST /api/v1/users/me/wallets/pay-booking` with `{"walletId", "bookingId"}`
**Then** My wallet balance becomes 500,000 VND, booking paymentStatus = SUCCESS, booking status = CONFIRMED, transaction recorded

### US-4: Insufficient Balance (Priority: P1)
As a Customer, I want the system to reject payment if I don't have enough funds, so I'm not surprised.

**Given** My wallet has balance 100,000 VND and I try to pay a 1,500,000 VND booking
**When** I call the pay-booking endpoint
**Then** I receive HTTP 400 with message `INSUFFICIENT_BALANCE`

### US-5: Refund Returns to Wallet (Priority: P2)
As a Customer, I want my refund credited back to my wallet, so I can rebook easily.

**Given** I paid a 1,000,000 VND booking using my wallet
**When** The booking is cancelled and refund policy applies (50%)
**Then** My wallet is credited 500,000 VND, transaction type=REFUND, balance increased

### US-6: Group Wallet Access (Priority: P2)
As a GroupOwner, I want to view my group's shared wallet balance, so I know the group's spending power.

**Given** I own a group with 5 members
**When** I call `GET /api/v1/users/me/wallets`
**Then** I see both my PERSONAL wallet and the GROUP wallet

### US-7: Admin Freezes Wallet (Priority: P2)
As an Admin, I want to freeze a suspicious wallet, so the user cannot spend during investigation.

**Given** A user's wallet is ACTIVE
**When** Admin calls `PATCH /admin/wallets/{id}/status` with `{ "status": "FROZEN", "reason": "Suspicious activity" }`
**Then** Wallet status = FROZEN, payment and deposit are blocked, refund still allowed

---

## 8. Acceptance Criteria

AC-046: Every user automatically gets a PERSONAL wallet on registration.
AC-047: Deposits, payments, and refunds are all recorded in `wallet_transactions` with running balance.
AC-048: Wallet payment deducts from balance atomically and updates booking status.
AC-049: Refund from a wallet-paid booking credits back to the same wallet.
AC-050: FROZEN wallets reject PAYMENT and DEPOSIT but allow REFUND.
AC-051: GroupOwner can manage shared GROUP wallet for the corporate group.
AC-052: Admins can freeze wallets and perform manual balance adjustments with audit trail.

---

## 9. Out of Scope

- Wallet-to-wallet transfer (no P2P)
- Crypto or multi-currency (VND only for Phase 1)
- Auto top-up (handled in spec 013)
- Spending limits per member (handled in spec 013)
- Loyalty cashback (handled in spec 011)
