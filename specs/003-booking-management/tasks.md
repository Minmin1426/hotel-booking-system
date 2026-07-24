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
- [x] T009 Write unit tests in `BookingServiceImplTest` verifying lock generation and overlap prevention

---

## Phase 3: Group Booking (>5 Rooms), Discounts & Corporate CTP
- [x] T010 Implement 25% automatic group discount logic in `BookingServiceImpl`
- [x] T011 Implement 30% partial deposit calculation and payment status transitions (`DEPOSIT_30_PAID`)
- [x] T012 Add Corporate Tax Profile (CTP) entity and repository for VAT invoice generation
- [x] T013 Implement Group Member Manifest with Excel (.xlsx) file import simulation widget in `HotelDetailPage.jsx`

---

## Phase 4: Expired Locks Cleanup & Background Schedulers
- [x] T014 Implement `RoomLockCleanupScheduler` using `@Scheduled` annotation firing every 60 seconds
- [x] T015 Implement lock release logic that deletes locks and marks corresponding pending bookings as `FAILED`
- [x] T016 Write unit tests in `RoomLockCleanupSchedulerTest` verifying background execution and expired record deletions

---

## Phase 5: Cancellation & E-Wallet Refund Engine
- [x] T017 Implement Cancellation Policy Engine calculating refund tiers (100%, 80%, 50%, 0%) based on check-in lead time
- [x] T018 Integrate automated refund crediting to Customer E-Wallet in `ProfilePage.jsx?tab=wallet`

---

## Phase 6: Frontend Integration & 50-Screen UI Alignment
- [x] T019 Build Group Booking Search tabs on `HotelsPage.jsx`
- [x] T020 Build Group Booking Calculator, Deposit Breakdown & Member Manifest on `HotelDetailPage.jsx`
- [x] T021 Build My Bookings history, Cancellation modal, and E-Wallet refund logs on `ProfilePage.jsx`
- [x] T022 Build Admin Booking Control Panel & Dynamic Lock Timeout Setting on `AdminDashboardPage.jsx`
- [x] T023 Run `npm run build` and `mvn test` to verify zero errors across full stack
