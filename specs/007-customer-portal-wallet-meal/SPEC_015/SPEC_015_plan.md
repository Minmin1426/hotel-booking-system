# Implementation Plan: 015-admin-customer-management

**Branch**: `015-admin-customer-management` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Build admin/receptionist operational view of all customers. 360° detail aggregating profile, loyalty, CTP, wallets, bookings, meal tickets, vouchers. Search, filter, VIP marking, internal notes, bulk actions, activity timeline.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA, Specifications for dynamic queries
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V28__Admin_customer_management.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.admin` (new) + extensions to `com.hotelbooking.user`
- **DTOs**: `CustomerListItem`, `CustomerDetail360Response`, `VipStatusRequest`, `CustomerNoteRequest`, `CustomerNoteResponse`, `CustomerActivityEventResponse`, `CustomerStatsResponse`, `BulkActionRequest`, `BulkActionResponse`
- **No business logic in controllers**: Aggregation queries and bulk action handling in `AdminCustomerServiceImpl`
- **Read-only most endpoints**: Only VIP, notes, lock, bulk actions are mutating

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V28__Admin_customer_management.sql`
  - Add `is_vip`, `vip_marked_at`, `vip_marked_by` to `users`
  - Create `customer_notes` table
  - Create `customer_activity_events` table (denormalized for fast timeline queries)
  - Create `bulk_action_logs` table
  - Indexes on `users.is_vip`, `customer_activity_events.user_id`

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/admin/CustomerNote.java`
- `src/main/java/com/hotelbooking/admin/CustomerNoteRepository.java`
- `src/main/java/com/hotelbooking/admin/CustomerActivityEvent.java`
- `src/main/java/com/hotelbooking/admin/CustomerActivityEventRepository.java`
- `src/main/java/com/hotelbooking/admin/BulkActionLog.java`
- `src/main/java/com/hotelbooking/admin/BulkActionLogRepository.java`
- Extend `src/main/java/com/hotelbooking/user/User.java` with `isVip`, `vipMarkedAt`, `vipMarkedBy`

#### Services
- `src/main/java/com/hotelbooking/admin/AdminCustomerService.java` — Interface
- `src/main/java/com/hotelbooking/admin/AdminCustomerServiceImpl.java` — List, detail, VIP, notes, activity, stats, bulk actions
- `src/main/java/com/hotelbooking/admin/CustomerActivityRecorder.java` — Utility for inserting activity events from various modules
- `src/main/java/com/hotelbooking/admin/CustomerSpecification.java` — Dynamic JPA Specification for search/filter

#### DTOs
- `src/main/java/com/hotelbooking/admin/dto/CustomerListItem.java`
- `src/main/java/com/hotelbooking/admin/dto/CustomerDetail360Response.java`
- `src/main/java/com/hotelbooking/admin/dto/VipStatusRequest.java`
- `src/main/java/com/hotelbooking/admin/dto/CustomerNoteRequest.java`
- `src/main/java/com/hotelbooking/admin/dto/CustomerNoteResponse.java`
- `src/main/java/com/hotelbooking/admin/dto/CustomerActivityEventResponse.java`
- `src/main/java/com/hotelbooking/admin/dto/CustomerStatsResponse.java`
- `src/main/java/com/hotelbooking/admin/dto/BulkActionRequest.java`
- `src/main/java/com/hotelbooking/admin/dto/BulkActionResponse.java`
- `src/main/java/com/hotelbooking/admin/dto/GroupMemberListItem.java`

#### Controllers
- `src/main/java/com/hotelbooking/admin/AdminCustomerController.java` — List, detail, VIP, notes, activity, stats, bulk actions
- Extend `src/main/java/com/hotelbooking/wallet/GroupController.java` (or new) — Group members list

#### Cross-Module Event Recording
- Hook `CustomerActivityRecorder` from payment, booking, wallet, voucher, loyalty, meal ticket modules to emit activity events
- Pattern: `customerActivityRecorder.record(userId, eventType, summary, metadata, actorUserId)`

### Testing
- `src/test/java/com/hotelbooking/admin/AdminCustomerServiceTest.java` — Search, filter, VIP, notes, bulk actions
- `src/test/java/com/hotelbooking/admin/AdminCustomerControllerTest.java` — Controller tests

## Non-Functional Requirements
- **Performance**: Customer list uses Specifications + indexes for fast filtering
- **Audit**: All admin actions (VIP, notes, bulk) recorded
- **Security**: Customer notes NEVER visible to customers themselves (separate endpoints)
- **Group scoping**: GroupOwner can only see their own group's members
