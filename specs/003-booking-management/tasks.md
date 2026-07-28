# Tasks: 003-booking-management

**Spec Reference**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)  
**Status**: All Tasks Completed & Production-Verified (100%)

---

## Phase 1: Stay Period Validation & Date Validation
- [x] T001 Define check-in/out date validation rules in DTO and Service layers
- [x] T002 Implement `/api/v1/bookings/validate-dates` in `BookingController`
- [x] T003 Write unit tests verifying past date rejection and checkout before check-in constraints

---

## Phase 2: Booking Core Transactions & Room Locking Engine
- [x] T004 Create `Booking` and `BookingRoom` database tables and JPA entities
- [x] T005 Create `RoomLock` database table and entity
- [x] T006 Implement `RoomLockServiceImpl` to write lock records on booking initiation
- [x] T007 Implement `@Transactional` booking creation in `BookingServiceImpl` checking availability & hotel room ownership
- [x] T008 Integrate `SystemSettingService` to read and update dynamic lock expiration times (10 to 30 mins)
- [x] T009 Implement `renewLock` endpoint (`PUT /api/v1/bookings/{id}/lock/renew`) to extend active room locks
- [x] T010 Write unit tests in `BookingServiceImplTest` verifying lock generation, renewal, and overlap prevention

---

## Phase 3: Group Booking (>5 Rooms), Discounts & Corporate CTP
- [x] T011 Implement 25% automatic group discount logic in `BookingServiceImpl`
- [x] T012 Implement 30% partial deposit calculation and payment status transitions (`DEPOSIT_30_PAID`)
- [x] T013 Add Corporate Tax Profile (CTP) entity and repository for VAT invoice generation
- [x] T014 Implement Group Member Manifest with Excel (.xlsx) file import simulation widget in `HotelDetailPage.jsx`

---

## Phase 4: Expired Locks Cleanup & Background Schedulers
- [x] T015 Implement `RoomLockCleanupScheduler` using `@Scheduled` annotation firing every 60 seconds
- [x] T016 Implement lock release logic that deletes locks and marks corresponding pending bookings as `FAILED`
- [x] T017 Write unit tests in `RoomLockCleanupSchedulerTest` verifying background execution and expired record deletions

---

## Phase 5: Cancellation & E-Wallet Refund Engine
- [x] T018 Implement Cancellation Policy Engine calculating refund tiers (100%, 80%, 50%, 0%) based on check-in lead time
- [x] T019 Integrate automated refund crediting to Customer E-Wallet in `ProfilePage.jsx?tab=wallet`

---

## Phase 6: Block / Batch Booking Engine via Excel (.xlsx) & Admin Control
- [x] T020 Implement `ExcelParsingService` for parsing `.xlsx` guest manifests and room requests
- [x] T021 Implement `BlockBookingRequest` and `BlockBookingRow` entities and repositories
- [x] T022 Implement `BatchBookingController` (`POST /api/v1/users/me/block-bookings`) for user batch uploads
- [x] T023 Implement `AdminBatchBookingController` (`/api/v1/admin/block-bookings`) for approving/rejecting requests
- [x] T024 Implement `AdminBookingController` (`/api/v1/admin/bookings`) for admin booking management CRUD and status updates

---

## Phase 7: Frontend Integration & 50-Screen UI Alignment
- [x] T025 Build Group Booking Search tabs on `HotelsPage.jsx`
- [x] T026 Build Group Booking Calculator, Deposit Breakdown & Member Manifest on `HotelDetailPage.jsx`
- [x] T027 Build My Bookings history, Cancellation modal, and E-Wallet refund logs on `ProfilePage.jsx`
- [x] T028 Build Admin Booking Control Panel, Block Booking approvals, & Dynamic Lock Timeout Setting on `AdminDashboardPage.jsx`
- [x] T029 Run `npm run build` and `mvn test` to verify zero errors across full stack
