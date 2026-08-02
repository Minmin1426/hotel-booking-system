# Implementation Plan: 008-block-booking-excel-import

**Branch**: `008-block-booking-excel-import` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Implement a batch booking upload system allowing CorporateMembers to upload .xlsx files with multiple guest bookings. The system validates rows, checks availability, creates a pending block request for admin approval, and converts approved batches into individual confirmed bookings.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA, Apache POI 5.2.5 (already in pom.xml)
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V21__Block_booking_excel_import.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.booking.batch` — new package for batch booking
- **Existing Dependencies**: Uses existing `Booking`, `Room`, `Hotel` entities; `BookingRepository`, `RoomRepository`, `HotelRepository`
- **DTOs**: `BatchUploadResponse`, `BatchRowValidationError`, `BlockBookingResponse`, `BatchStatusResponse`
- **No business logic in controllers**: All validation, availability checking, and booking creation in `BatchBookingServiceImpl`

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V21__Block_booking_excel_import.sql`
  - Create `block_booking_requests` table
  - Create `block_booking_rows` table
  - Index on `block_booking_requests.requester_id` and `block_booking_requests.status`

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/booking/batch/BlockBookingRequest.java`
- `src/main/java/com/hotelbooking/booking/batch/BlockBookingRow.java`
- `src/main/java/com/hotelbooking/booking/batch/BlockBookingRequestRepository.java`
- `src/main/java/com/hotelbooking/booking/batch/BlockBookingRowRepository.java`

#### Services
- `src/main/java/com/hotelbooking/booking/batch/BatchBookingService.java` — Interface
- `src/main/java/com/hotelbooking/booking/batch/BatchBookingServiceImpl.java` — Excel parsing, row validation, availability check, batch creation, approval/rejection logic
- `src/main/java/com/hotelbooking/booking/batch/ExcelParsingService.java` — Pure POI logic: read .xlsx, extract rows, map to DTOs

#### DTOs
- `src/main/java/com/hotelbooking/booking/batch/dto/BatchUploadResponse.java` — Summary after upload
- `src/main/java/com/hotelbooking/booking/batch/dto/BatchRowValidationError.java` — Per-row error
- `src/main/java/com/hotelbooking/booking/batch/dto/BatchDetailResponse.java` — Full batch detail
- `src/main/java/com/hotelbooking/booking/batch/dto/ApproveBatchResponse.java`
- `src/main/java/com/hotelbooking/booking/batch/dto/RejectBatchRequest.java`

#### Controllers
- `src/main/java/com/hotelbooking/booking/BatchBookingController.java` — Upload, list own batches, get detail
- Extend `src/main/java/com/hotelbooking/booking/BookingController.java` or create new admin controller

#### Admin Controller
- `src/main/java/com/hotelbooking/booking/AdminBatchBookingController.java` — List all, approve, reject

### Testing
- `src/test/java/com/hotelbooking/booking/batch/BatchBookingServiceTest.java` — Excel parsing, validation, availability check, approval flow
- `src/test/java/com/hotelbooking/booking/batch/BatchBookingControllerTest.java` — Controller tests

## Non-Functional Requirements
- **Security**: Only CORPORATE_MEMBER can upload; Admins/Receptionists can approve/reject. Ownership check on batch detail.
- **Performance**: Excel parsing with streaming for large files (> 100 rows). Availability check runs in-memory after DB fetch.
- **Data Integrity**: All booking creations in same transaction on approval.
- **Validation**: Apache POI for Excel parsing, Jakarta Bean Validation on parsed DTOs.
