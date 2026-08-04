# Feature Specification: 011-loyalty-membership-tiers

**Feature Branch**: `011-loyalty-membership-tiers`
**Created**: 2026-07-23
**Status**: Approved & Completed
**Primary Actor(s)**: Customer, CorporateMember, Admin
**Related Use Cases**: SCR-104

---

## 1. Context & Goal

A tiered loyalty program rewards customers based on cumulative booking spend. Individual customers progress through 4 tiers (Bronze, Silver, Gold, Platinum), while corporate members follow a parallel tier ladder (Bronze/Silver/Gold Business). Tier benefits include exclusive voucher access, spending limits, priority support, and bonus point multipliers. Tier promotions are evaluated automatically after each successful booking and can be manually adjusted by admins.

**Goal**: Implement a transparent, rules-based tier progression system with auto-evaluation, manual override, and tier-specific benefits.

---

## 2. Actors & Roles

**Customer** (CUSTOMER, individual): Earns loyalty points from bookings; progresses through individual tier ladder.
**CorporateMember** (CORPORATE_MEMBER): Earns loyalty points from company bookings; progresses through corporate tier ladder.
**Admin**: Can manually promote, demote, or freeze tier status for any user. Sets tier thresholds and benefits.
**System**: Evaluates tier after every booking confirmation. Awards points and updates tier.

---

## 3. Functional Requirements

### FR-001: Tier Definitions
THE system SHALL define 4 individual tiers and 4 corporate tiers:
- Individual: BRONZE, SILVER, GOLD, PLATINUM
- Corporate: BRONZE_BUSINESS, SILVER_BUSINESS, GOLD_BUSINESS, PLATINUM_BUSINESS
THE system SHALL default all new accounts to BRONZE / BRONZE_BUSINESS.

### FR-002: Tier Thresholds
Tier promotion SHALL be based on rolling 12-month booking spend (VND):
| Tier | Min 12-Month Spend | Point Multiplier |
|---|---|---|
| BRONZE | 0 | 1.0x |
| SILVER | 5,000,000 | 1.25x |
| GOLD | 20,000,000 | 1.5x |
| PLATINUM | 50,000,000 | 2.0x |
| BRONZE_BUSINESS | 0 | 1.0x |
| SILVER_BUSINESS | 30,000,000 | 1.5x |
| GOLD_BUSINESS | 100,000,000 | 2.0x |
| PLATINUM_BUSINESS | 500,000,000 | 3.0x |

### FR-003: Points Calculation
THE system SHALL award loyalty points on successful payment: `points = floor(paymentAmount × pointMultiplier)`.
THE system SHALL record points in a `loyalty_point_ledger` with running balance per user.

### FR-004: Tier Evaluation
WHEN a booking's payment status becomes `SUCCESS`, THE system SHALL:
1. Award points based on the user's current tier multiplier
2. Compute the user's rolling 12-month spend
3. Determine the new eligible tier
4. If new tier > current tier, promote user; record tier history
5. If new tier < current tier, demote user; record tier history

### FR-005: Tier History
THE system SHALL record every tier change in `tier_history` with: userId, previousTier, newTier, reason (`AUTO_PROMOTION`, `AUTO_DEMOTION`, `ADMIN_ADJUSTMENT`), changedBy, timestamp.

### FR-006: Tier Benefits
EACH tier SHALL define: `pointMultiplier`, `maxSpendingLimit`, `prioritySupport`, `exclusiveVoucherAccess`.
Default benefits per tier are configurable in `tier_definitions` table.

### FR-007: Manual Admin Adjustment
AN Admin SHALL be able to set a user's tier directly via `PUT /api/v1/admin/users/{id}/tier`.
Every manual adjustment SHALL create a tier_history entry with reason `ADMIN_ADJUSTMENT`.

### FR-008: View My Tier
THE system SHALL allow users to view their current tier, lifetime spend, lifetime points, and tier-specific benefits via `GET /api/v1/users/me/tier`.

---

## 4. Data Model

### Extended User Entity
| Field | Type | Description |
|---|---|---|
| current_tier | VARCHAR | Tier name (BRONZE, SILVER, etc.) |
| tier_evaluated_at | TIMESTAMP | Last evaluation timestamp |

### TierDefinition
| Field | Type | Description |
|---|---|---|
| tier_id | BIGINT | PK |
| name | VARCHAR | BRONZE, SILVER, GOLD, PLATINUM (and _BUSINESS) |
| account_type | VARCHAR | CUSTOMER or CORPORATE_MEMBER |
| min_annual_spend | DECIMAL(18,2) | Promotion threshold |
| point_multiplier | DECIMAL(3,2) | Points multiplier (1.0, 1.25, 1.5, 2.0) |
| max_spending_limit | DECIMAL(18,2) | Wallet top-up ceiling |
| priority_support | BOOLEAN | Whether tier gets priority support |
| exclusive_voucher_access | BOOLEAN | Access to tier-locked vouchers |
| created_at | TIMESTAMP | Auto-generated |

### LoyaltyPointLedger
| Field | Type | Description |
|---|---|---|
| ledger_id | BIGINT | PK |
| user_id | BIGINT | FK to users |
| booking_id | BIGINT | FK to bookings |
| points_earned | INT | Points for this transaction |
| multiplier_used | DECIMAL(3,2) | Multiplier applied |
| running_balance | BIGINT | User's total after this entry |
| created_at | TIMESTAMP | Auto-generated |

### TierHistory
| Field | Type | Description |
|---|---|---|
| history_id | BIGINT | PK |
| user_id | BIGINT | FK to users |
| previous_tier | VARCHAR | Previous tier |
| new_tier | VARCHAR | New tier |
| reason | VARCHAR | AUTO_PROMOTION, AUTO_DEMOTION, ADMIN_ADJUSTMENT |
| changed_by | BIGINT | FK to users (admin) — nullable for system changes |
| created_at | TIMESTAMP | Auto-generated |

---

## 5. API Contracts

### Get My Tier
```
GET /api/v1/users/me/tier
```
- **Auth**: Bearer token
- **Response 200**: `{ "tier", "pointMultiplier", "annualSpend", "lifetimePoints", "nextTier", "amountToNextTier", "benefits" }`

### Tier History
```
GET /api/v1/users/me/tier/history
```
- **Auth**: Bearer token
- **Response 200**: Paginated list of TierHistory entries

### Admin: Get Tier Definitions
```
GET /api/v1/admin/tier-definitions
```
- **Auth**: Bearer token (ADMIN)

### Admin: Update Tier Definition
```
PUT /api/v1/admin/tier-definitions/{tierId}
```
- **Auth**: Bearer token (ADMIN)

### Admin: Adjust User Tier
```
PUT /api/v1/admin/users/{userId}/tier
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "tier": "GOLD", "reason": "VIP customer - special request" }`
- **Response 200**: `{ "userId", "previousTier", "newTier", "changedAt" }`

### Admin: View User Points Ledger
```
GET /api/v1/admin/users/{userId}/points-ledger?page=0&size=20
```
- **Auth**: Bearer token (ADMIN)

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Invalid tier name | 400 Bad Request | INVALID_TIER |
| Admin setting tier to lower (without demotion reason) | 400 Bad Request | INVALID_DEMOTION |
| Tier definition not found | 404 Not Found | RESOURCE_NOT_FOUND |
| Non-admin adjusts tier | 403 Forbidden | ACCESS_DENIED |

---

## 7. User Scenarios & Testing

### US-1: Earn Points on Booking (Priority: P1)
As a Customer, I want to earn loyalty points when I book a room, so I can progress to higher tiers.

**Given** I am a BRONZE tier customer with multiplier 1.0x
**When** I complete a booking with payment of 1,000,000 VND
**Then** I earn 1,000,000 points, ledger entry created

### US-2: Auto Promotion to Silver (Priority: P1)
As a Customer, I want to be promoted to SILVER when my annual spend exceeds 5,000,000 VND, so I get better benefits.

**Given** I have accumulated 4,900,000 VND annual spend as BRONZE
**When** I complete a booking worth 200,000 VND
**Then** My tier becomes SILVER, multiplier updated to 1.25x, tier_history created

### US-3: Auto Demotion (Priority: P2)
As a System, I want to demote customers whose rolling 12-month spend drops below their current tier threshold, so tiers stay meaningful.

**Given** I am a SILVER tier customer with annual spend of 6,000,000 VND
**When** After 12 months pass without further bookings, my rolling spend drops to 3,000,000 VND
**Then** My tier is demoted to BRONZE, tier_history created with reason `AUTO_DEMOTION`

### US-4: Admin Manual Promotion (Priority: P2)
As an Admin, I want to promote a customer to PLATINUM manually for VIP treatment, so I can reward loyalty.

**Given** Customer has tier BRONZE
**When** Admin calls `PUT /admin/users/{id}/tier` with `{ "tier": "PLATINUM", "reason": "VIP customer" }`
**Then** Customer's tier becomes PLATINUM, multiplier 2.0x, history recorded

### US-5: View My Tier Info (Priority: P1)
As a Customer, I want to see my current tier and progress to the next tier, so I know how much more I need to spend.

**Given** I am SILVER with 12,000,000 VND annual spend
**When** I call `GET /api/v1/users/me/tier`
**Then** I see: currentTier=SILVER, multiplier=1.25x, annualSpend=12M, nextTier=GOLD, amountToNextTier=8M (need 20M for GOLD)

---

## 8. Acceptance Criteria

AC-040: Loyalty points are awarded on every successful payment based on the user's tier multiplier.
AC-041: Tier auto-evaluation runs after each successful payment, promoting/demoting as needed.
AC-042: Tier changes are recorded in `tier_history` with proper reason codes.
AC-043: Admins can manually adjust a user's tier; the change is logged.
AC-044: The customer's profile shows current tier, multiplier, lifetime points, and progress to next tier.
AC-045: New users default to BRONZE / BRONZE_BUSINESS.
