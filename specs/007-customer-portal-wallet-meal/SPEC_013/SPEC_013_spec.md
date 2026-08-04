# Feature Specification: 013-wallet-topup-spending-limits

**Feature Branch**: `013-wallet-topup-spending-limits`
**Created**: 2026-07-23
**Status**: Approved & Completed
**Primary Actor(s)**: Customer, CorporateMember, GroupOwner, Admin
**Related Use Cases**: SCR-106

---

## 1. Context & Goal

This module provides reusable top-up features (Stripe payment gateway integration) and per-member spending controls. Auto top-up triggers when balance drops below a threshold; manual top-up via Stripe Checkout. Spending limits can be set by GroupOwners to restrict how much each group member can spend per transaction, per day, or per month.

**Goal**: Make wallet funding frictionless and protect corporate group wallets from runaway member spending.

---

## 2. Actors & Roles

**Customer**: Configures auto top-up, makes manual top-ups, views top-up history.
**CorporateMember / GroupOwner**: Same as Customer PLUS configures per-member spending limits.
**Admin**: Configures global limits (max single top-up, max balance).

---

## 3. Functional Requirements

### FR-001: Manual Top-Up
THE system SHALL allow users to top up wallets via Stripe Checkout.
THE system SHALL generate a Stripe Checkout Session for the top-up amount.
THE system SHALL credit the wallet on Stripe webhook success.

### FR-002: Auto Top-Up
A user SHALL be able to configure auto top-up:
- `enabled` (boolean)
- `thresholdAmount` (when balance drops below, trigger top-up)
- `topUpAmount` (amount to add)
- `paymentMethodId` (Stripe payment method)
WHEN wallet balance < thresholdAmount, THE system SHALL automatically charge `topUpAmount`.
THE auto top-up SHALL respect a daily auto-topup limit (default 5 per day).

### FR-003: Top-Up Limits
THE system SHALL enforce `maxSingleTopUp` (default 50,000,000 VND per transaction).
THE system SHALL enforce `maxWalletBalance` (default 1,000,000,000 VND per wallet).
Admins can configure these limits per tier.

### FR-004: Spending Limits
A GroupOwner SHALL be able to set per-member spending limits via `PUT /api/v1/groups/{groupId}/members/{userId}/spending-limit`.
Spending limit types:
- `PER_TRANSACTION` (e.g., max 5,000,000 VND per booking)
- `DAILY` (max 20,000,000 VND across all bookings per day)
- `MONTHLY` (max 100,000,000 VND per calendar month)
A user with `spending_limit = NULL` SHALL have unlimited spending within the group's wallet.

### FR-005: Spending Limit Enforcement
WHEN a wallet payment is attempted, THE system SHALL check all applicable limits:
1. Group member's `PER_TRANSACTION` limit
2. Group member's `DAILY` cumulative spend (rolling 24h)
3. Group member's `MONTHLY` cumulative spend (current month)
WHERE any limit is exceeded, THE system SHALL reject with `SPENDING_LIMIT_EXCEEDED`.

### FR-006: Limit History
THE system SHALL record limit changes in `spending_limit_history` with: userId, groupId, previousLimit, newLimit, changedBy, reason, timestamp.

### FR-007: Top-Up History
EVERY top-up attempt (success or failure) SHALL be recorded in `topup_history` with: walletId, amount, paymentMethod, stripeSessionId, status, createdAt.

### FR-008: Auto Top-Up Webhook
WHEN the Stripe webhook for auto top-up succeeds, THE system SHALL credit the wallet, record `topup_history`, and send notification.
WHEN the auto top-up fails (e.g., insufficient Stripe funds), THE system SHALL log error and notify the user.

---

## 4. Data Model

### TopUpConfig
| Field | Type | Description |
|---|---|---|
| config_id | BIGINT | PK |
| user_id | BIGINT | FK to users |
| wallet_id | BIGINT | FK to wallets |
| enabled | BOOLEAN | Whether auto top-up is active |
| threshold_amount | DECIMAL(18,2) | Trigger threshold |
| topup_amount | DECIMAL(18,2) | Amount to add |
| payment_method_id | VARCHAR | Stripe payment method ID |
| max_daily_auto_topup | INT | Max auto top-ups per day (default 5) |
| created_at | TIMESTAMP | Auto-generated |
| updated_at | TIMESTAMP | Auto-updated |

### TopUpHistory
| Field | Type | Description |
|---|---|---|
| history_id | BIGINT | PK |
| wallet_id | BIGINT | FK to wallets |
| amount | DECIMAL(18,2) | Top-up amount |
| payment_method | VARCHAR | STRIPE, BANK_TRANSFER |
| stripe_session_id | VARCHAR | Stripe Checkout session ID |
| status | VARCHAR | PENDING, SUCCESS, FAILED |
| is_auto_topup | BOOLEAN | Whether triggered automatically |
| failure_reason | TEXT | Reason for failed top-up |
| created_at | TIMESTAMP | Auto-generated |

### SpendingLimit (extends GroupMembership from spec 012)
| Field | Type | Description |
|---|---|---|
| limit_id | BIGINT | PK |
| group_id | BIGINT | FK to groups |
| member_user_id | BIGINT | FK to users |
| per_transaction_limit | DECIMAL(18,2) | Nullable for unlimited |
| daily_limit | DECIMAL(18,2) | Nullable |
| monthly_limit | DECIMAL(18,2) | Nullable |
| effective_from | DATE | When this limit takes effect |
| effective_until | DATE | Nullable for open-ended |
| created_by | BIGINT | FK to users (GroupOwner or Admin) |
| created_at | TIMESTAMP | Auto-generated |

### SpendingLimitHistory
| Field | Type | Description |
|---|---|---|
| history_id | BIGINT | PK |
| limit_id | BIGINT | FK to spending_limits |
| previous_limit | DECIMAL(18,2) | Previous value |
| new_limit | DECIMAL(18,2) | New value |
| changed_by | BIGINT | FK to users |
| reason | TEXT | Reason for change |
| created_at | TIMESTAMP | Auto-generated |

### SpendingTracking (cumulative aggregation for quick lookup)
| Field | Type | Description |
|---|---|---|
| tracking_id | BIGINT | PK |
| user_id | BIGINT | FK to users |
| period_type | VARCHAR | DAILY or MONTHLY |
| period_start | DATE | Start of period |
| period_end | DATE | End of period |
| total_spent | DECIMAL(18,2) | Running total |

---

## 5. API Contracts

### Configure Auto Top-Up
```
PUT /api/v1/users/me/wallets/{walletId}/auto-topup
Content-Type: application/json
```
- **Auth**: Bearer token (wallet owner)
- **Request**: `{ "enabled", "thresholdAmount", "topupAmount", "paymentMethodId" }`
- **Response 200**: `{ "configId", "enabled", "thresholdAmount", "topupAmount" }`

### Manual Top-Up (Stripe Checkout)
```
POST /api/v1/users/me/wallets/{walletId}/topup
Content-Type: application/json
```
- **Auth**: Bearer token (wallet owner)
- **Request**: `{ "amount": 500000 }`
- **Response 200**: `{ "checkoutUrl": "https://checkout.stripe.com/...", "sessionId" }`

### Top-Up History
```
GET /api/v1/users/me/wallets/{walletId}/topup-history?page=0&size=20
```
- **Auth**: Bearer token (wallet owner)

### Set Member Spending Limit
```
PUT /api/v1/groups/{groupId}/members/{userId}/spending-limit
Content-Type: application/json
```
- **Auth**: Bearer token (GroupOwner)
- **Request**: `{ "perTransactionLimit": 5000000, "dailyLimit": 20000000, "monthlyLimit": 100000000, "effectiveFrom": "2026-07-25" }`
- **Response 200**: Updated limit object

### Get Member Spending Status
```
GET /api/v1/users/me/spending-status?groupId=
```
- **Auth**: Bearer token
- **Response 200**: `{ "perTransactionLimit", "dailyLimit", "dailySpent", "monthlyLimit", "monthlySpent", "remainingDaily", "remainingMonthly" }`

### Admin: Update Global Top-Up Limits
```
PUT /api/v1/admin/topup-limits
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "maxSingleTopUp": 50000000, "maxWalletBalance": 1000000000 }`

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Top-up amount > maxSingleTopUp | 400 Bad Request | TOPUP_AMOUNT_EXCEEDS_LIMIT |
| Wallet balance would exceed maxWalletBalance | 400 Bad Request | WALLET_BALANCE_LIMIT_EXCEEDED |
| Auto top-up Stripe charge fails | 200 OK | Status recorded as FAILED |
| Spending limit exceeded | 403 Forbidden | SPENDING_LIMIT_EXCEEDED |
| Invalid date range on spending limit | 400 Bad Request | INVALID_DATE_RANGE |
| Non-owner sets spending limit | 403 Forbidden | ACCESS_DENIED |
| Auto top-up already triggered today (max reached) | 429 Too Many Requests | AUTO_TOPUP_LIMIT_REACHED |

---

## 7. User Scenarios & Testing

### US-1: Manual Top-Up via Stripe (Priority: P1)
As a Customer, I want to add 500,000 VND to my wallet, so I have funds for my next booking.

**Given** My wallet has 100,000 VND
**When** I call `POST /api/v1/users/me/wallets/{id}/topup` with amount=500,000
**Then** I receive a Stripe Checkout URL, and upon payment my wallet has 600,000 VND

### US-2: Configure Auto Top-Up (Priority: P1)
As a Customer, I want auto top-up to trigger when my balance drops below 100,000 VND, so I never run out.

**Given** My wallet has 50,000 VND, threshold = 100,000, topup = 500,000
**When** My wallet balance drops to 50,000 VND (after a booking)
**Then** Auto top-up is triggered for 500,000 VND via Stripe, balance becomes 550,000 VND

### US-3: Auto Top-Up Daily Limit (Priority: P2)
As a System, I want to limit auto top-ups to 5 per day, so users don't get charged excessively.

**Given** Auto top-up has been triggered 5 times today
**When** My wallet balance drops again
**Then** Auto top-up is NOT triggered, error logged, user notified

### US-4: Set Spending Limit for Member (Priority: P1)
As a GroupOwner, I want to set a 5M VND per-transaction limit for a team member, so they cannot spend excessively.

**Given** I own a group with member A
**When** I call `PUT /api/v1/groups/{groupId}/members/{A.id}/spending-limit` with `perTransactionLimit = 5000000`
**Then** Member A's per-transaction limit is set, recorded in spending_limit_history

### US-5: Spending Limit Rejection (Priority: P1)
As a System, I want to reject wallet payments exceeding the member's spending limit, so corporate funds are protected.

**Given** Member A has per-transaction limit 5,000,000 VND
**When** Member A tries to pay a 6,000,000 VND booking using group wallet
**Then** HTTP 403 `SPENDING_LIMIT_EXCEEDED`

### US-6: Daily Cumulative Limit (Priority: P2)
As a System, I want to enforce daily cumulative spending, so members don't drain the wallet across multiple bookings.

**Given** Member A has daily limit 20,000,000 VND, has spent 18,000,000 VND today
**When** Member A tries another 5,000,000 VND payment
**Then** HTTP 403 `SPENDING_LIMIT_EXCEEDED` (would exceed daily total)

### US-7: Top-Up Exceeds Single Limit (Priority: P2)
As a Customer, I want to be told if my top-up exceeds the limit, so I don't proceed.

**Given** Max single top-up is 50,000,000 VND
**When** I try to top up 100,000,000 VND
**Then** HTTP 400 `TOPUP_AMOUNT_EXCEEDS_LIMIT`

---

## 8. Acceptance Criteria

AC-053: Manual top-up via Stripe Checkout credits the wallet on successful payment.
AC-054: Auto top-up triggers when balance drops below threshold.
AC-055: Auto top-up is limited to 5 times per day per wallet.
AC-056: Max single top-up and max wallet balance limits are enforced.
AC-057: GroupOwner can set per-member PER_TRANSACTION, DAILY, and MONTHLY spending limits.
AC-058: Wallet payment is rejected when any applicable spending limit is exceeded.
AC-059: Spending limit changes are recorded in `spending_limit_history`.
AC-060: Top-up attempts (success and failure) are recorded in `topup_history`.

---

## 9. Out of Scope

- Multiple payment methods per wallet (only Stripe for Phase 1)
- Crypto top-up
- Scheduled/recurring top-up (auto top-up based on threshold only)
- Spending limits for personal wallets (only group members in Phase 1)
- Per-hotel or per-room spending limits
