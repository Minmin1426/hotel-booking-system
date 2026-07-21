# Feature Specification: 005-inventory-catalog

**Feature Branch**: `005-inventory-catalog`

**Created**: 2026-06-23

**Status**: Completed

---

# 1. Context & Goal

## Purpose

The Inventory & Catalog feature enables administrators to manage hotel information, rooms, and hotel images. It also allows receptionists and housekeepers to update room availability and operational status.

This feature ensures that hotel information displayed to customers is accurate, up-to-date, and consistent with room availability.

## Business Goal

- Maintain an accurate hotel catalog.
- Ensure room inventory is always synchronized with operational status.
- Prevent inconsistent booking data caused by deleting hotels or rooms with active bookings.

---

# 2. Actors & Roles

| Actor | Description | Permissions |
|--------|-------------|-------------|
| Admin | System administrator | Create, update, soft-delete hotels, manage rooms, upload hotel images |
| Receptionist | Hotel staff | Update room availability and room status |
| Housekeeper | Hotel staff | Update room cleaning status and room availability |

Guests and Customers do not have permission to modify hotel or room information.

---

# 3. User Scenarios & Testing

## User Story 1 – Add/Edit Hotel (Priority: P1)

As an Admin,

I want to add new hotels and edit existing hotel information,

so that customers can view accurate hotel details.

### Why this priority

Hotel information must exist before rooms can be searched or booked.

### Independent Test

Create a hotel with valid information and verify it appears in the hotel catalog.

### Acceptance Scenarios

**Scenario 1**

Given an administrator

When valid hotel information is submitted

Then the hotel is created successfully.

**Scenario 2**

Given an existing hotel

When the administrator edits hotel information

Then the updated information is saved successfully.

---

## User Story 2 – Delete Hotel (Priority: P2)

As an Admin,

I want to remove hotels that are no longer operating,

so that customers cannot book unavailable hotels.

### Why this priority

Inactive hotels should not appear in search results while preserving booking history.

### Independent Test

Soft-delete a hotel and verify it no longer appears in hotel search.

### Acceptance Scenarios

**Scenario 1**

Given an active hotel

When the administrator deletes the hotel

Then the hotel status becomes inactive instead of being permanently deleted.

**Scenario 2**

Given a hotel with active bookings

When the administrator attempts to delete it

Then the system rejects the request.

---

## User Story 3 – Manage Rooms & Hotel Images (Priority: P2)

As an Admin,

I want to manage hotel rooms and hotel images,

so that customers receive complete hotel information.

### Why this priority

Room and image information is required for hotel search results and hotel detail pages.

### Independent Test

Create a room and upload a hotel image.

### Acceptance Scenarios

**Scenario 1**

Given a hotel

When a room is created

Then the room is linked to the correct hotel.

**Scenario 2**

Given a supported image file

When the administrator uploads the image

Then the image is successfully associated with the hotel.

---

## User Story 4 – Update Room Status (Priority: P2)

As a Receptionist or Housekeeper,

I want to update room operational status,

so that room availability accurately reflects real conditions.

### Why this priority

Room status directly affects hotel searching and booking.

### Independent Test

Update a room status and verify that the availability changes correctly.

### Acceptance Scenarios

**Scenario 1**

Given a dirty room

When cleaning is completed

Then the room status becomes AVAILABLE.

**Scenario 2**

Given a guest checks out

When the room is marked dirty

Then the room status becomes UNAVAILABLE.

---

# 4. Functional Requirements

### FR-001 Hotel Management

The system shall allow administrators to create, update, and soft-delete hotels.

### FR-002 Room Management

The system shall allow administrators to create, update, and manage hotel rooms.

### FR-003 Hotel Image Management

The system shall allow administrators to upload hotel images.

Supported image formats:

- JPG
- PNG
- WEBP

### FR-004 Room Status Management

Receptionists and Housekeepers shall update room status between:

- AVAILABLE
- UNAVAILABLE
- MAINTENANCE

### FR-005 Booking Protection

Hotels or rooms with active bookings shall not be permanently deleted.

### FR-006 Soft Delete

Deleting a hotel shall mark the hotel as inactive rather than removing database records.

---

# 5. Non-functional Requirements

### NFR-001 Reliability

Hotel and room information shall remain consistent after update operations.

### NFR-002 Security

Only authorized roles may modify hotel or room information.

### NFR-003 Data Integrity

The system shall prevent orphan booking records.

### NFR-004 File Validation

Only supported image formats shall be accepted.

---

# 6. Data Model

## Hotel

| Field | Description |
|--------|-------------|
| hotelId | Unique hotel identifier |
| name | Hotel name |
| location | Hotel location |
| description | Hotel description |
| rating | Hotel rating |
| isActive | Hotel status |

---

## Room

| Field | Description |
|--------|-------------|
| roomId | Room identifier |
| hotelId | Related hotel |
| roomNumber | Room number |
| type | Room type |
| price | Room price |
| status | AVAILABLE / UNAVAILABLE / MAINTENANCE |

---

## HotelImage

| Field | Description |
|--------|-------------|
| imageId | Image identifier |
| hotelId | Related hotel |
| imageUrl | Image location |

---

## Booking

| Field | Description |
|--------|-------------|
| bookingId | Booking identifier |
| roomId | Booked room |
| status | Booking status |

---

# 7. Error Handling

| Situation | Expected Result |
|------------|----------------|
| Invalid hotel information | Return validation error |
| Unsupported image format | Reject upload |
| Hotel not found | Return resource not found |
| Room not found | Return resource not found |
| Hotel has active bookings | Reject deletion |
| Unexpected server error | Return internal server error |

---

# 8. Acceptance Criteria

### AC-001

Administrators can successfully create hotels.

### AC-002

Administrators can successfully edit hotel information.

### AC-003

Hotels are soft-deleted instead of permanently removed.

### AC-004

Hotels with active bookings cannot be deleted.

### AC-005

Administrators can manage hotel rooms.

### AC-006

Supported hotel images can be uploaded.

### AC-007

Receptionists and Housekeepers can update room status.

### AC-008

Room availability reflects the latest operational status.

### AC-009

Only authorized users can modify hotel or room information.

### AC-010

Booking records remain valid after hotel updates.

---

# 9. Out of Scope

This feature does **not** include:

- Customer booking
- Payment processing
- User registration
- User authentication
- Hotel searching
- Room searching
- Review management
- Voucher management
- Email notification