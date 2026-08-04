# Tasks: 015-admin-customer-management

## Phase 1: Database & Entities
- [x] T001 Create Flyway migration `V28__Admin_customer_management.sql` — add `is_vip`, `vip_marked_at`, `vip_marked_by` to users; create `customer_notes`, `customer_activity_events`, `bulk_action_logs` tables
- [x] T002 Extend `User.java` with `isVip`, `vipMarkedAt`, `vipMarkedBy`
- [x] T003 Implement `CustomerNote` entity and repository
- [x] T004 Implement `CustomerActivityEvent` entity and repository
- [x] T005 Implement `BulkActionLog` entity and repository

## Phase 2: Customer Specification
- [x] T006 Implement `CustomerSpecification` — dynamic Specification for filtering by search/tier/accountType/status/ctpStatus/vip/dateRange
- [x] T007 Implement sort options (by totalSpend, by lastLoginAt, by createdAt)

## Phase 3: Admin Customer Service
- [x] T008 Implement `AdminCustomerService.listCustomers()` — paginated with Specification-based filtering
- [x] T009 Implement `AdminCustomerService.getCustomerDetail360()` — aggregate profile + loyalty + CTP + wallets + recent bookings + meal tickets + vouchers
- [x] T010 Implement `AdminCustomerService.setVipStatus()` — update user, record `customer_activity_events`
- [x] T011 Implement `AdminCustomerService.addNote()` / `updateNote()` / `deleteNote()` — staff notes CRUD
- [x] T012 Implement `AdminCustomerService.getActivityTimeline()` — paginated activity events
- [x] T013 Implement `AdminCustomerService.getStats()` — aggregate counts by tier, account type, VIP, etc.
- [x] T014 Implement `AdminCustomerService.executeBulkAction()` — handle SEND_NOTIFICATION, APPLY_TIER, APPLY_VOUCHER, LOCK_ACCOUNTS

## Phase 4: Group Members
- [x] T015 Implement group member listing (GroupOwner view) — reuse CustomerSpecification with `groupId` filter
- [x] T016 Ownership check — only GroupOwner of the group can list members

## Phase 5: Activity Recorder
- [x] T017 Implement `CustomerActivityRecorder.record()` — utility used by other modules
- [x] T018 Hook from `BookingServiceImpl` (booking created/cancelled/confirmed)
- [x] T019 Hook from `PaymentServiceImpl` (payment received)
- [x] T020 Hook from `WalletServiceImpl` (deposit, refund)
- [x] T021 Hook from `VoucherStoreServiceImpl` (voucher claimed, redeemed)
- [x] T022 Hook from `LoyaltyServiceImpl` (tier changed)
- [x] T023 Hook from `MealTicketServiceImpl` (ticket scanned)
- [x] T024 Hook from `AdminCustomerServiceImpl` itself (VIP, lock, bulk action)

## Phase 6: Controllers
- [x] T025 Create `AdminCustomerController` — list, detail, VIP, notes CRUD, activity, stats, bulk action
- [x] T026 Add group members endpoint — `GET /groups/{groupId}/members`
- [x] T027 RBAC: admin endpoints → ADMIN/RECEPTIONIST; group members → GroupOwner only

## Phase 7: Error Handling
- [x] T028 Add `CustomerNotFoundException`, `InvalidBulkActionException`, `InvalidNoteContentException` to GlobalExceptionHandler

## Phase 8: Testing
- [x] T029 Write `AdminCustomerServiceTest.java` — search/filter combinations, VIP marking, notes CRUD, bulk actions, stats aggregation
- [x] T030 Write `AdminCustomerControllerTest.java` — controller tests: unauthorized → 401, non-admin → 403, search returns results, VIP update returns 200
- [x] T031 Write `CustomerActivityRecorderTest.java` — verifies events are emitted with correct fields
- [x] T032 Verify all new acceptance criteria (AC-068 to AC-075) are covered
