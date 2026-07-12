# Tasks: 003-booking-management

**Input**: Design documents from `/specs/003-booking-management/`

## Phase 1: Stay Period Validation
- [x] T001 Define check-in/out date validation rules in DTO and Service layers
- [x] T002 Implement `/api/v1/bookings/validate-dates` in `BookingController`
- [x] T003 Write unit tests verifying past date rejection and checkout before check-in constraints

## Phase 2: Booking Core Transactions & Room Lock
- [x] T004 Create `Booking` and `BookingRoom` database tables and entities
- [x] T005 Create `RoomLock` database table and entity
- [x] T006 Implement `RoomLockServiceImpl` to write lock records on booking initiation
- [x] T007 Implement `@Transactional` booking creation in `BookingServiceImpl` checking availability first
- [x] T008 Integrate `SystemSettingService` to read the dynamic lock expiration time
- [x] T009 Write unit tests in `BookingServiceImplTest` verifying lock generation and overlap prevention

## Phase 3: Expired Locks Cleanup
- [x] T010 Implement `RoomLockCleanupScheduler` using `@Scheduled` annotation
- [x] T011 Implement lock release logic that deletes locks and marks corresponding pending bookings as `FAILED`
- [x] T012 Write unit tests in `RoomLockCleanupSchedulerTest` verifying background execution and expired record deletions

## Phase 4: Non-functional & Security Verification
- [x] T013 Verify JWT Authentication Filter is applied to all booking-related endpoints (except room search and `/api/v1/bookings/validate-dates`)
- [x] T014 Verify centralized `GlobalExceptionHandler` formats validation and state errors into standard JSON format
- [x] T015 Verify concurrency controls using database locks to prevent double-booking
