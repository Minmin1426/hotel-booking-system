# Feature Specification: 014-meal-ticket-wallet

**Feature Branch**: `014-meal-ticket-wallet`
**Created**: 2026-07-23
**Status**: Draft
**Primary Actor(s)**: Customer, CorporateMember, Receptionist, Admin
**Related Use Cases**: SCR-107

---

## 1. Context & Goal

Hotel guests receive meal tickets (buffet breakfast, lunch, dinner) as part of their bookings or loyalty tier benefits. Each meal ticket is represented as a QR code at the time of service, which the hotel restaurant scans to mark it as consumed. This module manages the meal ticket wallet for each user, tracks consumption, and integrates with the booking flow.

**Goal**: Provide a digital meal ticket system with QR codes, consumption tracking, and integration with bookings/tier benefits.

---

## 2. Actors & Roles

**Customer / CorporateMember**: Views their meal ticket wallet, presents QR code at restaurant.
**Receptionist**: Issues meal tickets manually (e.g., VIP guest, special request), prints QR codes.
**RestaurantStaff**: Scans QR codes to mark tickets as consumed.
**Admin**: Manages meal ticket types, pricing, and audit logs.

---

## 3. Functional Requirements

### FR-001: Meal Ticket Types
THE system SHALL support multiple meal ticket types: `BREAKFAST_BUFFET`, `LUNCH_BUFFET`, `DINNER_BUFFET`, `ROOM_SERVICE`, `MINIBAR_VOUCHER`, `SPA_VOUCHER`.
A meal ticket type has: `code`, `name`, `description`, `defaultValidDays`, `defaultPrice` (cost when purchased standalone).

### FR-002: Meal Ticket Issuance
Meal tickets SHALL be issued through:
1. **Booking inclusion** — When a booking includes meal tickets, they are auto-issued on booking confirmation
2. **Loyalty benefit** — Tier-based free meal tickets (e.g., PLATINUM gets 1 free breakfast/day)
3. **Manual issuance** — Receptionist issues tickets for special cases
4. **Corporate bulk** — CorporateMember issues tickets to team members

### FR-003: Meal Ticket Record
A `MealTicket` record SHALL have: `ticketId`, `userId`, `bookingId` (nullable), `ticketType`, `qrCode` (unique), `status` (UNUSED, USED, EXPIRED, CANCELLED), `issuedAt`, `expiresAt`, `usedAt`, `consumedByStaffId` (nullable).

### FR-004: QR Code Generation
WHEN a meal ticket is issued, THE system SHALL generate a unique QR code containing:
- `ticketId` (encrypted)
- `userId` (encrypted)
- `signature` (HMAC-SHA256 with server secret)
THE QR code SHALL be returned as a base64-encoded PNG image or a scannable string.

### FR-005: QR Code Consumption
WHEN a RestaurantStaff scans a QR code via `POST /api/v1/restaurant/scan-ticket`:
1. THE system SHALL verify the QR signature
2. THE system SHALL check the ticket is `UNUSED` and not expired
3. THE system SHALL mark it `USED`, record `usedAt` and `consumedByStaffId`
4. THE system SHALL return confirmation with ticket details

### FR-006: Expiry
WHERE `now > expiresAt`, THE system SHALL mark the ticket as `EXPIRED`.
A scheduled job SHALL run daily to update expired tickets.

### FR-007: Meal Ticket Wallet
THE system SHALL allow users to view all their meal tickets via `GET /api/v1/users/me/meal-tickets`.
Filters: status (UNUSED, USED, EXPIRED), ticketType, date range.

### FR-008: Corporate Bulk Issuance
A CorporateMember with appropriate role SHALL be able to issue meal tickets to all members of their group via `POST /api/v1/groups/{groupId}/meal-tickets/issue`.

---

## 4. Data Model

### MealTicketType
| Field | Type | Description |
|---|---|---|
| type_id | BIGINT | PK |
| code | VARCHAR | BREAKFAST_BUFFET, etc. |
| name | VARCHAR | Display name |
| description | TEXT | Description |
| default_valid_days | INT | Days until expiry (default 30) |
| default_price | DECIMAL(18,2) | Standalone price |

### MealTicket
| Field | Type | Description |
|---|---|---|
| ticket_id | BIGINT | PK |
| user_id | BIGINT | FK to users (ticket holder) |
| booking_id | BIGINT | FK to bookings, nullable |
| ticket_type | VARCHAR | FK to MealTicketType.code |
| qr_code | VARCHAR | Unique QR string |
| qr_signature | VARCHAR | HMAC-SHA256 signature |
| status | VARCHAR | UNUSED, USED, EXPIRED, CANCELLED |
| issued_at | TIMESTAMP | When created |
| expires_at | TIMESTAMP | When it expires |
| used_at | TIMESTAMP | When scanned, nullable |
| consumed_by_staff_id | BIGINT | FK to users (staff), nullable |
| issued_by | BIGINT | FK to users (who issued), nullable |
| created_at | TIMESTAMP | Auto-generated |

### MealTicketAuditLog
| Field | Type | Description |
|---|---|---|
| audit_id | BIGINT | PK |
| ticket_id | BIGINT | FK to meal_tickets |
| action | VARCHAR | ISSUED, SCANNED, EXPIRED, CANCELLED |
| actor_user_id | BIGINT | FK to users (who performed) |
| timestamp | TIMESTAMP | Auto-generated |
| metadata | TEXT | JSON metadata |

---

## 5. API Contracts

### List My Meal Tickets
```
GET /api/v1/users/me/meal-tickets?status=UNUSED&type=&page=0&size=20
```
- **Auth**: Bearer token
- **Response 200**: Paginated list of MealTicket with QR code data

### Get Meal Ticket QR Image
```
GET /api/v1/users/me/meal-tickets/{ticketId}/qr
```
- **Auth**: Bearer token (ticket owner)
- **Response 200**: PNG image of QR code (base64)

### Restaurant: Scan QR Code (Consume Ticket)
```
POST /api/v1/restaurant/scan-ticket
Content-Type: application/json
```
- **Auth**: Bearer token (STAFF role)
- **Request**: `{ "qrCode": "<scanned-string>" }`
- **Response 200**: `{ "ticketId", "userId", "userFullName", "ticketType", "status": "USED", "consumedAt" }`

### Receptionist: Issue Manual Ticket
```
POST /api/v1/admin/meal-tickets/issue
Content-Type: application/json
```
- **Auth**: Bearer token (RECEPTIONIST or ADMIN)
- **Request**: `{ "userId", "ticketType", "validDays": 30, "notes": "VIP guest" }`

### Corporate: Bulk Issue to Group
```
POST /api/v1/groups/{groupId}/meal-tickets/issue
Content-Type: application/json
```
- **Auth**: Bearer token (CorporateMember, GroupOwner)
- **Request**: `{ "ticketType", "validDays", "memberIds": [1,2,3] }`

### Admin: List Ticket Types
```
GET /api/v1/admin/meal-ticket-types
```
- **Auth**: Bearer token (ADMIN)

### Admin: Create/Update Ticket Type
```
POST /api/v1/admin/meal-ticket-types
PUT /api/v1/admin/meal-ticket-types/{typeId}
```

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Invalid QR signature | 401 Unauthorized | INVALID_QR_CODE |
| Ticket already used | 400 Bad Request | TICKET_ALREADY_USED |
| Ticket expired | 400 Bad Request | TICKET_EXPIRED |
| Ticket not found | 404 Not Found | TICKET_NOT_FOUND |
| Non-staff scans QR | 403 Forbidden | ACCESS_DENIED |
| Non-receptionist issues ticket | 403 Forbidden | ACCESS_DENIED |

---

## 7. User Scenarios & Testing

### US-1: View Meal Ticket Wallet (Priority: P1)
As a Customer, I want to see all my unused meal tickets, so I can use them at the restaurant.

**Given** I have 3 unused breakfast tickets and 1 used dinner ticket
**When** I call `GET /api/v1/users/me/meal-tickets?status=UNUSED`
**Then** I see 3 unused tickets with QR codes

### US-2: Auto-Issue on Booking (Priority: P1)
As a Customer, I want my booking's included breakfast tickets to appear in my wallet, so I don't miss them.

**Given** I have a CONFIRMED booking that includes BREAKFAST_BUFFET × 3 nights
**When** My booking is confirmed
**Then** 3 BREAKFAST_BUFFET tickets are auto-issued, each with unique QR codes

### US-3: Scan and Consume Ticket (Priority: P1)
As a RestaurantStaff, I want to scan a guest's QR code, so the breakfast is marked as served.

**Given** A guest presents a QR code for an unused breakfast ticket
**When** I scan it via `POST /api/v1/restaurant/scan-ticket`
**Then** The ticket is marked USED, my staff ID is recorded, response confirms

### US-4: Expired Ticket Rejection (Priority: P1)
As a System, I want to reject expired tickets, so guests can't use stale tickets.

**Given** A ticket expired 5 days ago
**When** Staff scans the QR
**Then** HTTP 400 with message `TICKET_EXPIRED`

### US-5: Already-Used Ticket Rejection (Priority: P2)
As a System, I want to reject tickets that have already been used, so tickets can't be reused.

**Given** A ticket was scanned yesterday
**When** Staff scans the same QR again today
**Then** HTTP 400 with message `TICKET_ALREADY_USED`

### US-6: Bulk Issue to Corporate Group (Priority: P2)
As a GroupOwner, I want to issue lunch tickets to all 10 team members for an event, so they can all attend.

**Given** I own a group with 10 members
**When** I call `POST /api/v1/groups/{groupId}/meal-tickets/issue` with `ticketType=LUNCH_BUFFET`
**Then** 10 tickets are created, one per member, all visible in their wallets

### US-7: Tier-Based Free Ticket (Priority: P2)
As a PLATINUM customer, I want a free breakfast ticket each day of my stay, so I get my loyalty benefit.

**Given** I am PLATINUM tier with a 5-night booking
**When** My booking is confirmed
**Then** 5 BREAKFAST_BUFFET tickets are issued automatically as tier benefits

---

## 8. Acceptance Criteria

AC-061: Meal tickets are issued on booking confirmation (when included) with unique QR codes.
AC-062: QR codes are signed with HMAC-SHA256 to prevent forgery.
AC-063: Staff can scan QR codes to mark tickets as consumed.
AC-064: Used and expired tickets cannot be re-scanned.
AC-065: Corporate groups can bulk-issue meal tickets to all members.
AC-066: Tier-based free tickets are auto-issued (e.g., PLATINUM gets free breakfast).
AC-067: Every ticket operation is recorded in `meal_ticket_audit_log`.

---

## 9. Out of Scope

- Third-party POS integration (Phase 2 — assume direct QR scan to our API)
- Online ordering via tickets (Phase 2)
- Ticket transfer between users (no P2P)
- Refunds for unused tickets after expiry (no cash value)
- Multi-restaurant chain support (single hotel for Phase 1)
