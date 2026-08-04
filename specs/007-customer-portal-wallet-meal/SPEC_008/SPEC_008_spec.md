# Feature Specification: 008-block-booking-excel-import

**Feature Branch**: `008-block-booking-excel-import`
**Created**: 2026-07-23
**Status**: Approved & Completed
**Primary Actor(s)**: CorporateMember, Admin, Receptionist
**Related Use Cases**: SCR-103

---

## 1. Context & Goal

Corporate members often need to book multiple rooms for their employees or event attendees in a single transaction. This module allows CorporateMembers to upload an Excel (.xlsx) file containing a list of guests, check-in/check-out dates, and room preferences, which the system validates and converts into individual booking requests. An Admin or Receptionist reviews and approves the batch before rooms are committed.

**Goal**: Eliminate manual, one-by-one booking for corporate block reservations. Reduce booking errors through Excel validation. Provide an audit trail for corporate booking batches.

---

## 2. Actors & Roles

**CorporateMember** (role = CORPORATE_MEMBER, account_type = CORPORATE_MEMBER): Uploads the Excel file and tracks batch status.
**Admin/Receptionist**: Reviews, approves, or rejects the batch booking requests.
**System**: Validates the Excel file, checks room availability, creates booking records.

---

## 3. Functional Requirements

### FR-001: Excel File Upload
THE system SHALL accept `.xlsx` files uploaded via `POST /api/v1/bookings/batch/upload`.
THE system SHALL validate the Excel structure: required columns are `guestName`, `email`, `phoneNumber`, `hotelId`, `checkInDate`, `checkOutDate`, `roomType`, `quantity`.
THE system SHALL reject files with missing required columns, wrong format, or non-.xlsx extension with HTTP 400.

### FR-002: Row-Level Validation
THE system SHALL validate each row independently:
- `guestName`: non-empty string, max 100 chars
- `email`: valid email format
- `phoneNumber`: valid phone format (10-15 digits)
- `hotelId`: must exist in the database
- `checkInDate`: must be today or in the future
- `checkOutDate`: must be after `checkInDate`
- `roomType`: must be a valid room type for the hotel
- `quantity`: positive integer (1–10)
THE system SHALL group validation errors by row number and return a summary of all errors without creating any bookings.

### FR-003: Availability Check
WHEN all row validations pass, THE system SHALL check room availability for each row:
- Rooms matching the `roomType` at the `hotelId` must be available for the full date range
- The total requested `quantity` must be available
THE system SHALL mark unavailable rows in the error summary.

### FR-004: Batch Booking Creation
WHERE all validations pass, THE system SHALL create a `BlockBookingRequest` record with status `PENDING_APPROVAL`.
THE system SHALL create individual `Booking` records for each Excel row, all linked to the same `blockRequestId`.
THE system SHALL set each booking status to `PENDING` (awaiting payment or admin confirmation).
THE system SHALL compute and store the total batch amount.

### FR-005: Batch Status Lifecycle
`PENDING_APPROVAL` → `APPROVED` → rooms locked for each booking → `CONFIRMED` bookings
`PENDING_APPROVAL` → `REJECTED` → no bookings created, all soft-deleted
`APPROVED` → `CANCELLED` → individual bookings cancelled, refund triggered
An Admin or Receptionist can approve or reject a `PENDING_APPROVAL` batch.

### FR-006: CorporateMember Views Own Batches
THE system SHALL allow CorporateMembers to list their own block booking requests via `GET /api/v1/users/me/batch-bookings`.
CorporateMembers cannot view or manage batches created by other users.

### FR-007: Admin/Receptionist Batch Management
THE system SHALL allow Admins/Receptionists to list all block booking requests via `GET /api/v1/admin/batch-bookings`.
THE system SHALL allow Admins/Receptionists to approve or reject via `POST /api/v1/admin/batch-bookings/{id}/approve` and `POST /api/v1/admin/batch-bookings/{id}/reject`.

---

## 4. Data Model

### BlockBookingRequest
| Field | Type | Description |
|---|---|---|
| block_booking_id | BIGINT | PK, auto-increment |
| requester_id | BIGINT | FK to users |
| file_name | VARCHAR | Original uploaded file name |
| total_guests | INT | Number of rows in the file |
| total_amount | DECIMAL(18,2) | Sum of all booking amounts |
| status | VARCHAR | `PENDING_APPROVAL`, `APPROVED`, `REJECTED`, `CANCELLED` |
| rejection_reason | TEXT | Reason if rejected |
| approved_by | BIGINT | FK to users (admin/receptionist) |
| approved_at | TIMESTAMP | When approved/rejected |
| created_at | TIMESTAMP | Auto-generated |
| updated_at | TIMESTAMP | Auto-updated |

### BlockBookingRow (intermediate, embedded or separate table)
| Field | Type | Description |
|---|---|---|
| row_id | BIGINT | PK |
| block_booking_id | BIGINT | FK to BlockBookingRequest |
| guest_name | VARCHAR | Guest full name |
| email | VARCHAR | Guest email |
| phone_number | VARCHAR | Guest phone |
| hotel_id | BIGINT | FK to hotels |
| check_in_date | DATE | Check-in date |
| check_out_date | DATE | Check-out date |
| room_type | VARCHAR | Room type name |
| quantity | INT | Number of rooms |
| booking_id | BIGINT | FK to bookings (nullable until approved) |
| row_status | VARCHAR | `VALID`, `INVALID`, `BOOKED` |
| error_message | TEXT | Validation error if any |
| created_at | TIMESTAMP | Auto-generated |

---

## 5. API Contracts

### Upload Excel Batch Booking
```
POST /api/v1/bookings/batch/upload
Content-Type: multipart/form-data
```
- **Auth**: Bearer token (CORPORATE_MEMBER)
- **Request**: `file` (MultipartFile .xlsx)
- **Response 200**: `{ "blockBookingId", "totalGuests", "validRows", "invalidRows", "status": "PENDING_APPROVAL", "totalAmount", "errors": [{ "row": 3, "field": "email", "message": "Invalid email format" }] }`

### List My Batch Bookings
```
GET /api/v1/users/me/batch-bookings?page=0&size=10
```
- **Auth**: Bearer token (CORPORATE_MEMBER)
- **Response 200**: Paginated list of BlockBookingRequest summaries

### Get Batch Booking Detail
```
GET /api/v1/users/me/batch-bookings/{blockBookingId}
```
- **Auth**: Bearer token (CORPORATE_MEMBER, owner only)
- **Response 200**: Full detail with row-by-row status

### Admin: List All Batch Bookings
```
GET /api/v1/admin/batch-bookings?status=PENDING_APPROVAL&page=0&size=20
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Response 200**: Paginated list

### Admin: Approve Batch
```
POST /api/v1/admin/batch-bookings/{blockBookingId}/approve
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Response 200**: `{ "blockBookingId", "status": "APPROVED", "bookingsCreated": 10, "message" }`

### Admin: Reject Batch
```
POST /api/v1/admin/batch-bookings/{blockBookingId}/reject
```
- **Auth**: Bearer token (ADMIN or RECEPTIONIST)
- **Request**: `{ "reason": "Hotel not available for requested dates" }`
- **Response 200**: `{ "blockBookingId", "status": "REJECTED", "message" }`

---

## 6. Error Handling

| Condition | HTTP Status | Message |
|---|---|---|
| File is not .xlsx | 400 Bad Request | INVALID_FILE_FORMAT |
| Excel missing required columns | 400 Bad Request | MISSING_REQUIRED_COLUMNS |
| All rows have validation errors | 400 Bad Request | ALL_ROWS_INVALID |
| Any room unavailable | 200 OK (batch created with partial errors) | Availability warnings in response |
| Block booking not found | 404 Not Found | RESOURCE_NOT_FOUND |
| Non-corporate user uploads batch | 403 Forbidden | ACCESS_DENIED |
| Non-owner views batch detail | 403 Forbidden | ACCESS_DENIED |
| Admin approves already-approved batch | 400 Bad Request | BATCH_ALREADY_PROCESSED |

---

## 7. User Scenarios & Testing

### US-1: Successful Excel Upload (Priority: P1)
As a CorporateMember, I want to upload a valid Excel file with 10 guest rows, so that the system creates a batch booking request for admin approval.

**Given** a valid .xlsx file with all required columns and valid data
**When** submitting to `POST /api/v1/bookings/batch/upload`
**Then** a `BlockBookingRequest` with status `PENDING_APPROVAL` is created, 10 rows are stored, and the total amount is computed

### US-2: Partial Validation Errors (Priority: P1)
As a CorporateMember, I want to see row-level validation errors, so I can fix the Excel and re-upload.

**Given** an Excel file where rows 3 and 5 have invalid emails
**When** submitting the file
**Then** rows 3 and 5 are marked `INVALID` with error messages, rows 1,2,4,6-10 are `VALID`, and the batch is still created with only valid rows processed

### US-3: Availability Check Failure (Priority: P1)
As a CorporateMember, I want the system to tell me when rooms are unavailable, so I can adjust my request.

**Given** an Excel file where the requested room type has only 2 rooms but the file requests quantity=5
**When** submitting the file
**Then** the affected rows are marked with `error_message = "Only X rooms available for requested dates"` and the batch is still created

### US-4: Admin Approves Batch (Priority: P1)
As an Admin, I want to approve a block booking request, so rooms are committed for the corporate client.

**Given** a `BlockBookingRequest` with status `PENDING_APPROVAL`
**When** Admin calls `POST /api/v1/admin/batch-bookings/{id}/approve`
**Then** status → `APPROVED`, individual bookings are created with status `CONFIRMED`, room locks are created

### US-5: Admin Rejects Batch (Priority: P2)
As an Admin, I want to reject a block booking with a reason, so the CorporateMember knows why.

**Given** a `BlockBookingRequest` with status `PENDING_APPROVAL`
**When** Admin calls `POST /api/v1/admin/batch-bookings/{id}/reject` with a reason
**Then** status → `REJECTED`, rejection reason stored, no bookings created

---

## 8. Acceptance Criteria

AC-015: Valid .xlsx upload creates a `PENDING_APPROVAL` BlockBookingRequest with individual row records.
AC-016: Row-level validation errors are returned in the response with row numbers and field names.
AC-017: Room availability is checked for each row and availability failures are reported.
AC-018: Only Admins/Receptionists can approve or reject block booking requests.
AC-019: CorporateMembers can only view their own batch bookings.
AC-020: Approval creates individual `CONFIRMED` bookings linked to the block request.

---

## 9. Out of Scope

- Multi-room-type per guest (one row = one room type, use multiple rows for multi-room needs)
- Automatic payment for batch bookings (individual payment flow per booking, or manual admin confirmation)
- Email notifications to guests on approval
- Excel template download endpoint
- Partial batch approval (all-or-nothing for Phase 1)
