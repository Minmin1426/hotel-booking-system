# Tasks: 008-block-booking-excel-import

**Input**: Design documents from `/specs/008-block-booking-excel-import/`

## Phase 1: Database & Entities
- [x] T001 Create Flyway migration `V21__Block_booking_excel_import.sql` — `block_booking_requests` and `block_booking_rows` tables with all specified columns and indexes
- [x] T002 Implement `BlockBookingRequest` JPA entity with all fields
- [x] T003 Implement `BlockBookingRow` JPA entity with all fields
- [x] T004 Implement `BlockBookingRequestRepository` and `BlockBookingRowRepository`

## Phase 2: Excel Parsing Service
- [x] T005 Create `ExcelParsingService` — uses Apache POI to read .xlsx, map rows to intermediate DTOs, validate column presence
- [x] T006 Validate file extension is .xlsx
- [x] T007 Handle empty file, single-row file edge cases

## Phase 3: Batch Booking Service
- [x] T008 Implement `BatchBookingService.uploadExcel()` — parse Excel, validate each row, check availability, create `BlockBookingRequest` with `PENDING_APPROVAL` status, create `BlockBookingRow` records
- [x] T009 Row-level validation: guestName, email, phone, hotelId, dates, roomType, quantity
- [x] T010 Availability check: query rooms by hotelId+roomType, exclude booked and locked rooms, compare with quantity
- [x] T011 Compute total batch amount (sum of room prices × quantity × nights)
- [x] T012 Implement `BatchBookingService.getBatchDetail()` — return full detail with row-by-row status for batch owner
- [x] T013 Implement `BatchBookingService.listMyBatches()` — paginated list for authenticated CorporateMember
- [x] T014 Implement `BatchBookingService.approveBatch()` — create individual `Booking` records, room locks, update batch status to `APPROVED`
- [x] T015 Implement `BatchBookingService.rejectBatch()` — update status to `REJECTED`, store reason

## Phase 4: Controllers
- [x] T016 Create `BatchBookingController` — `POST /api/v1/bookings/batch/upload` (multipart), `GET /api/v1/users/me/batch-bookings`, `GET /api/v1/users/me/batch-bookings/{id}`
- [x] T017 Create `AdminBatchBookingController` — `GET /api/v1/admin/batch-bookings`, `POST /api/v1/admin/batch-bookings/{id}/approve`, `POST /api/v1/admin/batch-bookings/{id}/reject`
- [x] T018 Secure endpoints: upload → CORPORATE_MEMBER, admin endpoints → ADMIN/RECEPTIONIST, list/detail → owner only
- [x] T019 Configure multipart resolver in `application.properties` if not already present

## Phase 5: Error Handling & Edge Cases
- [x] T020 Handle missing required columns → 400 with `MISSING_REQUIRED_COLUMNS`
- [x] T021 Handle all rows invalid → 400 with `ALL_ROWS_INVALID`
- [x] T022 Handle non-corporate user uploading → 403
- [x] T023 Handle non-owner accessing batch detail → 403
- [x] T024 Handle already-processed batch approval/rejection → 400 `BATCH_ALREADY_PROCESSED`

## Phase 6: Testing
- [x] T025 Write `BatchBookingServiceTest.java` — unit tests: parse valid Excel, handle missing columns, row validation errors, availability failure, approval creates bookings, rejection updates status
- [x] T026 Write `BatchBookingControllerTest.java` — controller tests: unauthorized → 403, corporate uploads → 200, admin approves → 200
- [x] T027 Verify all new acceptance criteria (AC-015 to AC-020) are covered by tests
