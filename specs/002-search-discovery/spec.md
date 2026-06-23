# Feature Specification: 002-search-discovery

**Feature Branch**: `002-search-discovery`
**Created**: 2026-06-23
**Status**: Completed

## User Scenarios & Testing

### User Story 1 - Search Hotels (Priority: P1)
As a Guest or Customer, I want to search for hotels by location and name, so I can see what options are available.

**Why this priority**: Essential first step in booking a hotel.
**Independent Test**: Search for hotels using GET `/api/v1/hotels/search?location=Hanoi` and check the returned list.

**Acceptance Scenarios**:
1. **Given** hotels exist in location "Hanoi", **When** searching for location "Hanoi", **Then** the list of Hanoi hotels is returned with HTTP 200 OK.
2. **Given** a large list of hotels, **When** searching, **Then** results are paginated (default 20 per page).

### User Story 2 - Filter & Sort Hotels (Priority: P2)
As a Guest or Customer, I want to filter and sort hotels by rating, price, and amenities, to find the best option for me.

**Why this priority**: Enhances the discovery experience.
**Independent Test**: Filter hotels by rating range or sort by price ascending.

**Acceptance Scenarios**:
1. **Given** a set of hotels, **When** applying a rating filter (e.g. rating >= 4.0), **Then** only hotels matching the criteria are returned.

### User Story 3 - View Room Availability (Priority: P1)
As a Guest or Customer, I want to view room availability in a hotel for my specific check-in and check-out dates.

**Why this priority**: Directly blocks the booking flow. Must know if a room is free.
**Independent Test**: Retrieve available rooms for hotel ID 1 between 2026-07-10 and 2026-07-15.

**Acceptance Scenarios**:
1. **Given** a hotel with rooms, **When** querying room availability for a date range, **Then** only rooms that are not booked or temporarily locked during those dates are returned.

## Requirements

### Functional Requirements
- **FR-001**: Search API must support pagination (page, size, sort).
- **FR-002**: Room availability queries must check both finalized bookings (`booking` status `CONFIRMED`) and active room locks (`room_locks` table).
- **FR-003**: Inactive or soft-deleted hotels/rooms must not appear in search results.

### Key Entities
- **Hotel**: Represents hotel details. Fields: `hotelId`, `name`, `location`, `description`, `isActive`, `rating`.
- **Room**: Represents individual rooms in a hotel. Fields: `roomId`, `hotelId`, `roomNumber`, `type`, `price`, `isActive`.

## Success Criteria
- **SC-001**: Hotel search and room availability check completed under 500ms.
- **SC-002**: Correct real-time availability checking, excluding locked/booked rooms.
