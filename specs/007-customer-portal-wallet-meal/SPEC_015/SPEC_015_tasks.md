# Tasks: 015-admin-customer-management

## Phase 1: Database & Entities
- [ ] T001 Create Flyway migration `V28__Admin_customer_management.sql` — add `is_vip`, `vip_marked_at`, `vip_marked_by` to users; create `customer_notes`, `customer_activity_events`, `bulk_action_logs` tables
- [ ] T002 Extend `User.java` with `isVip`, `vipMarkedAt`, `vipMarkedBy`
- [ ] T003 Implement `CustomerNote` entity and repository
- [ ] T004 Implement `CustomerActivityEvent` entity and repository
- [ ] T005 Implement `BulkActionLog` entity and repository

## Phase 2: Customer Specification
- [ ] T006 Implement `CustomerSpecification` — dynamic Specification for filtering by search/tier/accountType/status/ctpStatus/vip/dateRange
- [ ] T007 Implement sort options (by totalSpend, by lastLoginAt, by createdAt)

## Phase 3: Admin Customer Service
- [ ] T008 Implement `AdminCustomerService.listCustomers()` — paginated with Specification-based filtering
- [ ] T009 Implement `AdminCustomerService.getCustomerDetail360()` — aggregate profile + loyalty + CTP + wallets + recent bookings + meal tickets + vouchers
- [ ] T010 Implement `AdminCustomerService.setVipStatus()` — update user, record `customer_activity_events`
- [ ] T011 Implement `AdminCustomerService.addNote()` / `updateNote()` / `deleteNote()` — staff notes CRUD
- [ ] T012 Implement `AdminCustomerService.getActivityTimeline()` — paginated activity events
- [ ] T013 Implement `AdminCustomerService.getStats()` — aggregate counts by tier, account type, VIP, etc.
- [ ] T014 Implement `AdminCustomerService.executeBulkAction()` — handle SEND_NOTIFICATION, APPLY_TIER, APPLY_VOUCHER, LOCK_ACCOUNTS

## Phase 4: Group Members
- [ ] T015 Implement group member listing (GroupOwner view) — reuse CustomerSpecification with `groupId` filter
- [ ] T016 Ownership check — only GroupOwner of the group can list members

## Phase 5: Activity Recorder
- [ ] T017 Implement `CustomerActivityRecorder.record()` — utility used by other modules
- [ ] T018 Hook from `BookingServiceImpl` (booking created/cancelled/confirmed)
- [ ] T019 Hook from `PaymentServiceImpl` (payment received)
- [ ] T020 Hook from `WalletServiceImpl` (deposit, refund)
- [ ] T021 Hook from `VoucherStoreServiceImpl` (voucher claimed, redeemed)
- [ ] T022 Hook from `LoyaltyServiceImpl` (tier changed)
- [ ] T023 Hook from `MealTicketServiceImpl` (ticket scanned)
- [ ] T024 Hook from `AdminCustomerServiceImpl` itself (VIP, lock, bulk action)

## Phase 6: Controllers
- [ ] T025 Create `AdminCustomerController` — list, detail, VIP, notes CRUD, activity, stats, bulk action
- [ ] T026 Add group members endpoint — `GET /groups/{groupId}/members`
- [ ] T027 RBAC: admin endpoints → ADMIN/RECEPTIONIST; group members → GroupOwner only

## Phase 7: Error Handling
- [ ] T028 Add `CustomerNotFoundException`, `InvalidBulkActionException`, `InvalidNoteContentException` to GlobalExceptionHandler

## Phase 8: Testing
- [ ] T029 Write `AdminCustomerServiceTest.java` — search/filter combinations, VIP marking, notes CRUD, bulk actions, stats aggregation
- [ ] T030 Write `AdminCustomerControllerTest.java` — controller tests: unauthorized → 401, non-admin → 403, search returns results, VIP update returns 200
- [ ] T031 Write `CustomerActivityRecorderTest.java` — verifies events are emitted with correct fields
- [ ] T032 Verify all new acceptance criteria (AC-068 to AC-075) are covered
