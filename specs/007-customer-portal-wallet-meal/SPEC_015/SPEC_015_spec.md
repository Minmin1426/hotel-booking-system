# Feature Specification: 015-admin-customer-management

**Feature Branch**: `015-admin-customer-management`
**Created**: 2026-07-23
**Status**: Draft
**Primary Actor(s)**: Admin, Receptionist, CorporateMember
**Related Use Cases**: SCR-110

---

## 1. Context & Goal

Hotel staff need a unified admin view to manage individual customers, corporate groups, and VIP accounts. This module provides search, filter, view-detail, and management actions for the full customer base. It ties together customer profiles, wallets, loyalty tiers, vouchers, meal tickets, and bookings into a single 360-degree view for staff use.

**Goal**: Give Admins and Receptionists a complete operational view of every customer account, with fast search and bulk actions.

---

## 2. Actors & Roles

**Admin**: Full access — can view, edit, freeze, and delete any customer account. Can manage VIP status, override tier, freeze wallets.
**Receptionist**: Read access + limited actions — can view customer details, issue meal tickets, cancel bookings on behalf of customer, send notifications.
**CorporateMember (GroupOwner)**: View-only access to their group's members (subset of admin view, scoped to own group).

---

## 3. Functional Requirements

### FR-001: Customer List with Search and Filter
THE system SHALL provide `GET /api/v1/admin/customers` with filters:
- `search`: full-text search on email, fullName, phoneNumber, identificationNumber
- `accountType`: CUSTOMER or CORPORATE_MEMBER
- `tier`: BRONZE/SILVER/GOLD/PLATINUM (and _BUSINESS variants)
- `status`: ACTIVE/LOCKED/INACTIVE
- `ctpStatus`: PENDING/VERIFIED/REJECTED (for corporate)
- `vip`: boolean filter
- `dateRange`: createdAt, lastLoginAt
- `page`, `size`, `sort` (e.g., by totalSpend DESC, by lastLoginAt DESC)

### FR-002: Customer 360° Detail View
THE system SHALL provide `GET /api/v1/admin/customers/{userId}` returning a comprehensive view:
- Basic profile (name, email, phone, account type, tier, status)
- Loyalty summary (current tier, points, lifetime spend)
- CTP status (for corporate)
- Wallet balance (personal + group, if applicable)
- Recent bookings (last 5)
- Active meal tickets count
- Claimed vouchers count
- Recent login activity (from login_audit_logs)
- VIP status and special notes

### FR-003: VIP Marking
AN Admin SHALL be able to mark a customer as VIP via `PUT /api/v1/admin/customers/{userId}/vip-status`.
VIP status grants: priority support, dedicated account manager, exclusive vouchers.
A `VIP` flag on the user SHALL be visible in the admin list filter.

### FR-004: Customer Notes
AN Admin or Receptionist SHALL be able to add internal notes about a customer (e.g., preferences, complaints, special requests).
Notes are NOT visible to the customer; only staff.
Notes have: `noteId`, `userId`, `authorId`, `content`, `createdAt`, `updatedAt`.

### FR-005: Bulk Actions
AN Admin SHALL be able to perform bulk actions:
- Send broadcast notification to selected customers
- Apply tier promotion to selected customers
- Apply voucher to selected customers
- Lock selected accounts
`POST /api/v1/admin/customers/bulk-action` with `{ "customerIds": [...], "action": "SEND_NOTIFICATION", "payload": {...} }`

### FR-006: Customer Activity Timeline
THE system SHALL aggregate customer activity into a timeline:
- Bookings created/cancelled/confirmed
- Payments received
- Wallet deposits/withdrawals
- Voucher claims/redemptions
- Tier changes
- Login events
- Meal ticket scans
- Admin actions (tier override, VIP marking)
`GET /api/v1/admin/customers/{userId}/activity?page=0&size=20`

### FR-007: Customer Statistics
THE system SHALL provide aggregate stats:
- Total customers by tier (BRONZE: 50, SILVER: 30, ...)
- Total customers by account type
- Active customers (logged in within 30 days)
- New customers this month
- VIP count
`GET /api/v1/admin/customers/stats`

### FR-008: Lock/Unlock Customer Account
AN Admin SHALL be able to lock a customer account (extending existing `PATCH /admin/users/{id}/status`).
THE system SHALL record the reason in `login_audit_logs` and notify the customer via email.

### FR-009: View Group Members (for Corporate)
A GroupOwner SHALL see their group members via `GET /api/v1/groups/{groupId}/members` with the same filters as admin (limited to own group).
A GroupOwner SHALL NOT see other groups.

---

## 4. Data Model

### Extended User Entity
| Field | Type | Description |
|---|---|---|
| is_vip | BOOLEAN | VIP flag |
| vip_marked_at | TIMESTAMP | When VIP status granted |
| vip_marked_by | BIGINT | FK to users (admin) |

### CustomerNote
| Field | Type | Description |
|---|---|---|
| note_id | BIGINT | PK |
| user_id | BIGINT | FK to users (subject) |
| author_id | BIGINT | FK to users (admin/receptionist who wrote) |
| content | TEXT | Note content |
| is_pinned | BOOLEAN | Pinned to top |
| created_at | TIMESTAMP | Auto-generated |
| updated_at | TIMESTAMP | Auto-updated |

### CustomerActivityEvent (denormalized for fast timeline queries)
| Field | Type | Description |
|---|---|---|
| event_id | BIGINT | PK |
| user_id | BIGINT | FK to users |
| event_type | VARCHAR | BOOKING_CREATED, PAYMENT_RECEIVED, WALLET_DEPOSIT, VOUCHER_CLAIMED, TIER_CHANGED, etc. |
| event_summary | VARCHAR | Short text |
| event_metadata | TEXT | JSON details |
| actor_user_id | BIGINT | FK to users (nullable) |
| created_at | TIMESTAMP | Auto-generated |

### BulkActionLog
| Field | Type | Description |
|---|---|---|
| bulk_action_id | BIGINT | PK |
| admin_id | BIGINT | FK to users |
| action_type | VARCHAR | SEND_NOTIFICATION, LOCK_ACCOUNTS, etc. |
| target_user_ids | TEXT | Comma-separated user IDs |
| payload | TEXT | JSON payload |
| created_at | TIMESTAMP | Auto-generated |

---

## 5. API Contracts

### List Customers (Admin)
```
GET /api/v1/admin/customers?search=&tier=&accountType=&page=0&size=20&sort=lastLoginAt,desc
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Response 200**: Paginated list with summary fields

### Customer Detail (360° View)
```
GET /api/v1/admin/customers/{userId}
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Response 200**: Full detail with all related data

### Set VIP Status
```
PUT /api/v1/admin/customers/{userId}/vip-status
Content-Type: application/json
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "isVip": true }`
- **Response 200**: `{ "userId", "isVip", "vipMarkedAt", "vipMarkedBy" }`

### Add Customer Note
```
POST /api/v1/admin/customers/{userId}/notes
Content-Type: application/json
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Request**: `{ "content": "Prefers high-floor rooms", "isPinned": true }`
- **Response 201**: Note object

### Customer Activity Timeline
```
GET /api/v1/admin/customers/{userId}/activity?type=&page=0&size=20
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Response 200**: Paginated activity events

### Customer Statistics
```
GET /api/v1/admin/customers/stats
```
- **Auth**: Bearer token (ADMIN)
- **Response 200**: Aggregate counts

### Bulk Action
```
POST /api/v1/admin/customers/bulk-action
Content-Type: application/json
```
- **Auth**: Bearer token (ADMIN)
- **Request**: `{ "customerIds": [1,2,3], "action": "SEND_NOTIFICATION", "payload": { "subject": "Promotion", "body": "..." } }`
- **Response 200**: `{ "bulkActionId", "affectedCount" }`

### List Group Members (Corporate)
```
GET /api/v1/groups/{groupId}/members?search=&page=0&size=20
```
- **Auth**: Bearer token (GroupOwner)
- **Response 200**: Paginated members

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| Non-admin/non-receptionist access | 403 Forbidden | ACCESS_DENIED |
| Customer not found | 404 Not Found | CUSTOMER_NOT_FOUND |
| Invalid bulk action | 400 Bad Request | INVALID_BULK_ACTION |
| Empty customerIds list | 400 Bad Request | NO_CUSTOMERS_SELECTED |
| Note content empty | 400 Bad Request | INVALID_NOTE_CONTENT |
| Non-owner viewing group members | 403 Forbidden | ACCESS_DENIED |

---

## 7. User Scenarios & Testing

### US-1: Search Customers by Email (Priority: P1)
As an Admin, I want to search for a customer by email, so I can quickly find their account.

**Given** 1000 customers exist
**When** I call `GET /api/v1/admin/customers?search=nguyen@gmail.com`
**Then** I see customers matching that email

### US-2: Filter VIP Customers (Priority: P1)
As an Admin, I want to see all VIP customers, so I can manage them.

**Given** VIP customers exist
**When** I call `GET /api/v1/admin/customers?vip=true`
**Then** I see only VIP-flagged customers

### US-3: Customer 360° View (Priority: P1)
As an Admin, I want to see all info about a customer in one place, so I can assist them.

**Given** Customer X has bookings, wallet balance, loyalty points, meal tickets
**When** I call `GET /api/v1/admin/customers/{X.id}`
**Then** I see all related data in one response

### US-4: Mark Customer as VIP (Priority: P2)
As an Admin, I want to mark a frequent customer as VIP, so they get priority treatment.

**Given** Customer X has 50 bookings
**When** I call `PUT /api/v1/admin/customers/{X.id}/vip-status` with `{"isVip": true}`
**Then** Customer X is flagged as VIP, vip_marked_at is recorded, vip_marked_by is my user ID

### US-5: Add Customer Note (Priority: P2)
As a Receptionist, I want to add a note about a customer's preferences, so my colleagues are informed.

**Given** Customer Y always requests a quiet room
**When** I call `POST /api/v1/admin/customers/{Y.id}/notes` with `{"content": "Prefers quiet room", "isPinned": true}`
**Then** A note is created, pinned at top of customer detail

### US-6: Bulk Send Notification (Priority: P2)
As an Admin, I want to send a promotion notification to all GOLD tier customers, so they come back.

**Given** 30 GOLD tier customers exist
**When** I call `POST /api/v1/admin/customers/bulk-action` with `{"customerIds": [...], "action": "SEND_NOTIFICATION", "payload": {...}}`
**Then** All 30 customers receive the notification, bulk_action_log created

### US-7: View Group Members (Priority: P2)
As a GroupOwner, I want to see my group members, so I can manage the team.

**Given** I own a group with 5 members
**When** I call `GET /api/v1/groups/{groupId}/members`
**Then** I see my 5 members only (not other groups)

### US-8: Customer Activity Timeline (Priority: P2)
As an Admin, I want to see what a customer did recently, so I can debug issues.

**Given** Customer Z has 10 bookings, 20 wallet transactions, 5 voucher claims
**When** I call `GET /api/v1/admin/customers/{Z.id}/activity`
**Then** I see all events in chronological order

---

## 8. Acceptance Criteria

AC-068: Admins can search customers by email, name, phone, or ID number.
AC-069: Admins can filter customers by tier, account type, status, CTP status, VIP flag.
AC-070: Customer 360° view returns profile, loyalty, CTP, wallet, bookings, meal tickets, and vouchers.
AC-071: VIP status can be set/unset by admins with proper audit.
AC-072: Admin notes can be added, pinned, and updated.
AC-073: Bulk actions are logged in `bulk_action_log`.
AC-074: Activity timeline aggregates events from all modules.
AC-075: GroupOwners can only view members of their own group.

---

## 9. Out of Scope

- Customer self-service (only admin views — customers use their own endpoints)
- Customer-to-customer communication
- AI-based segmentation or recommendations
- Customer satisfaction surveys (separate module)
- Marketing automation workflows
- Real-time WebSocket activity feed (Phase 2)
