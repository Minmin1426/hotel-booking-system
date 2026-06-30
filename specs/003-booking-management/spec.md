# Feature Specification: 003-booking-management

**Feature Branch**: `003-booking-management`
**Created**: 2026-06-23
**Status**: Completed

## User Scenarios & Testing

### User Story 1 - Create Booking & Validate Dates (Priority: P1)
As a Customer, I want to select check-in and check-out dates, validate availability, and book a room, so I can reserve my stay.

**Why this priority**: Core transaction of the hotel booking system.
**Independent Test**: Request `/api/v1/bookings` (POST) with valid room IDs and stay dates to successfully create a PENDING booking.

**Acceptance Scenarios**:
1. **Given** a customer selects stay dates, **When** check-in date is in the past, **Then** stay period validation fails with a validation error.
2. **Given** a customer books a room, **When** the room is currently available, **Then** a booking is created with status `PENDING`.
3. **Given** a booking is created, **When** the payment process is initiated, **Then** the room is temporarily locked (Room Lock) for 10 minutes.

### User Story 2 - Confirm & Cancel Bookings (Priority: P1)
As a Customer, Receptionist, or Admin, I want to confirm, cancel, or process bookings to keep availability and check-in statuses accurate.

**Why this priority**: Required to manage reservation lifecycles, handle offline/manual bookings, and trigger refund policies.
**Independent Test**: Cancel a confirmed booking via `/api/v1/bookings/{id}/cancel` or process an offline booking via `/api/v1/admin/bookings/{id}/status`.

**Acceptance Scenarios**:
1. **Given** a PENDING booking, **When** payment succeeds or receptionist confirms offline payment, **Then** the booking status changes to `CONFIRMED`.
2. **Given** a CONFIRMED booking, **When** cancelling before check-in, **Then** booking status changes to `CANCELLED` and refund is scheduled.
3. **Given** a receptionist, **When** creating an offline booking for a guest, **Then** a booking is successfully created and managed under admin endpoints.

### User Story 3 - Room Lock & Automatic Release (Priority: P2)
As the System, I want to lock rooms during payment processing and automatically release them if payment fails or expires.

**Why this priority**: Avoids double-booking (race conditions) during concurrent checkouts.
**Independent Test**: Create a booking, wait for 10 minutes without payment, and verify that the scheduler releases the room lock and cancels the booking.

**Acceptance Scenarios**:
1. **Given** an active room lock of 10 minutes, **When** the scheduler runs after 10 minutes, **Then** the lock is deleted and the booking is marked `FAILED` or `CANCELLED`.

## Requirements

### Functional Requirements
- **FR-001**: Stay periods must be validated: check-in date >= today, check-out date > check-in date.
- **FR-002**: Room Lock duration must be configurable via settings (default 10 minutes).
- **FR-003**: System must use a cron-based scheduler (`RoomLockCleanupScheduler`) to release expired locks.
- **FR-004**: System must use database transactions to prevent concurrent bookings for the same room.

### Key Entities
- **Booking**: Represents a reservation. Fields: `bookingId`, `userId`, `hotelId`, `totalPrice`, `status` (PENDING, CONFIRMED, CANCELLED, FAILED).
- **BookingRoom**: Intermediate entity mapping bookings to rooms with the historical price.
- **RoomLock**: Represents temporary locks. Fields: `lockId`, `roomId`, `bookingId`, `lockedUntil`, `createdAt`.
- **SystemSetting**: Configuration key-value pairs (e.g. `room_lock_duration_minutes`).

## Success Criteria
- **SC-001**: Clean validation rules fail with a 400 Bad Request error.
- **SC-002**: Zero double bookings under concurrent checkout requests.
