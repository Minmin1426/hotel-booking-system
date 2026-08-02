# Tasks: 008-block-booking-excel-import

**Input**: Design documents from `/specs/008-block-booking-excel-import/`

## Phase 1: Database & Entities
- [ ] T001 Create Flyway migration `V21__Block_booking_excel_import.sql` — `block_booking_requests` and `block_booking_rows` tables with all specified columns and indexes
- [ ] T002 Implement `BlockBookingRequest` JPA entity with all fields
- [ ] T003 Implement `BlockBookingRow` JPA entity with all fields
- [ ] T004 Implement `BlockBookingRequestRepository` and `BlockBookingRowRepository`

## Phase 2: Excel Parsing Service
- [ ] T005 Create `ExcelParsingService` — uses Apache POI to read .xlsx, map rows to intermediate DTOs, validate column presence
- [ ] T006 Validate file extension is .xlsx
- [ ] T007 Handle empty file, single-row file edge cases

## Phase 3: Batch Booking Service
- [ ] T008 Implement `BatchBookingService.uploadExcel()` — parse Excel, validate each row, check availability, create `BlockBookingRequest` with `PENDING_APPROVAL` status, create `BlockBookingRow` records
- [ ] T009 Row-level validation: guestName, email, phone, hotelId, dates, roomType, quantity
- [ ] T010 Availability check: query rooms by hotelId+roomType, exclude booked and locked rooms, compare with quantity
- [ ] T011 Compute total batch amount (sum of room prices × quantity × nights)
- [ ] T012 Implement `BatchBookingService.getBatchDetail()` — return full detail with row-by-row status for batch owner
- [ ] T013 Implement `BatchBookingService.listMyBatches()` — paginated list for authenticated CorporateMember
- [ ] T014 Implement `BatchBookingService.approveBatch()` — create individual `Booking` records, room locks, update batch status to `APPROVED`
- [ ] T015 Implement `BatchBookingService.rejectBatch()` — update status to `REJECTED`, store reason

## Phase 4: Controllers
- [ ] T016 Create `BatchBookingController` — `POST /api/v1/bookings/batch/upload` (multipart), `GET /api/v1/users/me/batch-bookings`, `GET /api/v1/users/me/batch-bookings/{id}`
- [ ] T017 Create `AdminBatchBookingController` — `GET /api/v1/admin/batch-bookings`, `POST /api/v1/admin/batch-bookings/{id}/approve`, `POST /api/v1/admin/batch-bookings/{id}/reject`
- [ ] T018 Secure endpoints: upload → CORPORATE_MEMBER, admin endpoints → ADMIN/RECEPTIONIST, list/detail → owner only
- [ ] T019 Configure multipart resolver in `application.properties` if not already present

## Phase 5: Error Handling & Edge Cases
- [ ] T020 Handle missing required columns → 400 with `MISSING_REQUIRED_COLUMNS`
- [ ] T021 Handle all rows invalid → 400 with `ALL_ROWS_INVALID`
- [ ] T022 Handle non-corporate user uploading → 403
- [ ] T023 Handle non-owner accessing batch detail → 403
- [ ] T024 Handle already-processed batch approval/rejection → 400 `BATCH_ALREADY_PROCESSED`

## Phase 6: Testing
- [ ] T025 Write `BatchBookingServiceTest.java` — unit tests: parse valid Excel, handle missing columns, row validation errors, availability failure, approval creates bookings, rejection updates status
- [ ] T026 Write `BatchBookingControllerTest.java` — controller tests: unauthorized → 403, corporate uploads → 200, admin approves → 200
- [ ] T027 Verify all new acceptance criteria (AC-015 to AC-020) are covered by tests
