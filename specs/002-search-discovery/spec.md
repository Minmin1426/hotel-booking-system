# Feature Specification: 002-search-discovery

**Feature Branch**: `002-search-discovery`

**Created**: 2026-06-23

**Status**: Completed

---

# 1. Context & Goal

## Purpose

The Search & Discovery feature enables users to search hotels, filter search results, and check room availability before making a booking.

This feature is the first step in the hotel booking workflow. It helps users quickly find suitable hotels while ensuring only available rooms are displayed.

## Business Goal

- Improve hotel discovery experience.
- Reduce booking failures caused by unavailable rooms.
- Support fast and accurate hotel searching.

---

# 2. Actors & Roles

| Actor | Description | Permissions |
|--------|-------------|-------------|
| Guest | Visitor who has not logged in | Search hotels, filter hotels, view hotel details, check room availability |
| Customer | Registered user | Same permissions as Guest before making a booking |

Authentication is not required for hotel searching or room availability checking.

---

# 3. User Scenarios & Testing

## User Story 1 – Search Hotels (Priority: P1)

As a Guest or Customer,

I want to search hotels by location or hotel name,

so that I can easily find hotels matching my destination.

### Why this priority

Searching hotels is the first step before any booking can happen.

### Independent Test

Search hotels using the Hotel Search API with a location keyword.

### Acceptance Scenarios

#### Scenario 1

**Given**

Hotels exist in Hanoi.

**When**

The user searches for "Hanoi".

**Then**

The system returns all active hotels located in Hanoi.

---

#### Scenario 2

**Given**

Hotels exist whose names contain "Grand".

**When**

The user searches by hotel name.

**Then**

The system returns matching hotels only.

---

#### Scenario 3

**Given**

More hotels exist than one page can display.

**When**

The user requests another page.

**Then**

The system returns only the hotels belonging to that page.

---

## User Story 2 – Filter & Sort Hotels (Priority: P2)

As a Guest or Customer,

I want to filter and sort hotels,

so that I can quickly find hotels matching my preferences.

### Why this priority

Filtering improves the hotel discovery experience.

### Independent Test

Apply rating filter and sort hotels by price.

### Acceptance Scenarios

#### Scenario 1

**Given**

Hotels have different ratings.

**When**

The user filters rating >= 4.0.

**Then**

Only hotels satisfying the condition are returned.

---

#### Scenario 2

**Given**

Hotels have different room prices.

**When**

The user sorts by price ascending.

**Then**

Hotels are displayed from lowest to highest price.

---

## User Story 3 – View Room Availability (Priority: P1)

As a Guest or Customer,

I want to view available rooms during my selected stay,

so that I can choose a room that can actually be booked.

### Why this priority

Room availability directly affects whether booking can continue.

### Independent Test

Select a hotel and retrieve available rooms for a selected check-in and check-out date.

### Acceptance Scenarios

#### Scenario 1

**Given**

Rooms exist for the selected hotel.

**When**

The user checks availability.

**Then**

Only available rooms are returned.

---

#### Scenario 2

**Given**

Some rooms are already booked.

**When**

Availability is checked.

**Then**

Booked rooms are excluded.

---

#### Scenario 3

**Given**

Some rooms are temporarily locked.

**When**

Availability is checked.

**Then**

Locked rooms are excluded.

---

# 4. Functional Requirements

### FR-001 Hotel Search

The system shall allow users to search hotels using:

- hotel name
- location

---

### FR-002 Hotel Filtering

The system shall support filtering hotels by:

- rating
- price range
- amenities (if available)

---

### FR-003 Hotel Sorting

The system shall support sorting search results by:

- price
- rating

---

### FR-004 Pagination

The system shall support paginated search results.

---

### FR-005 Active Hotels

Inactive hotels shall not appear in search results.

---

### FR-006 Active Rooms

Inactive rooms shall not appear when checking room availability.

---

### FR-007 Room Availability

The system shall determine room availability by excluding:

- confirmed bookings
- active room locks

---

### FR-008 Hotel Information

Each search result shall include:

- hotel name
- location
- rating
- thumbnail image
- minimum room price

---

# 5. Non-functional Requirements

## NFR-001 Performance

The search feature should return results quickly under normal operating conditions.

---

## NFR-002 Scalability

The system shall support pagination to reduce database load.

---

## NFR-003 Reliability

Search results shall accurately reflect the current hotel and room status.

---

## NFR-004 Security

Only publicly accessible hotel information shall be returned.

No sensitive internal information shall be exposed.

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
| isActive | Hotel availability status |

---

## Room

| Field | Description |
|--------|-------------|
| roomId | Unique room identifier |
| hotelId | Hotel reference |
| roomNumber | Room number |
| type | Room type |
| price | Room price |
| isActive | Room availability status |

---

## Booking

| Field | Description |
|--------|-------------|
| bookingId | Booking identifier |
| roomId | Booked room |
| checkIn | Check-in date |
| checkOut | Check-out date |
| status | Booking status |

---

## RoomLock

| Field | Description |
|--------|-------------|
| roomId | Locked room |
| lockStart | Lock start date |
| lockEnd | Lock end date |
| expiresAt | Lock expiration |

---

# 7. Error Handling

| Situation | Expected Result |
|------------|----------------|
| No hotel matches search | Return an empty list |
| Invalid search parameters | Return validation error |
| Invalid date range | Return validation error |
| Hotel does not exist | Return resource not found |
| Unexpected server error | Return internal server error |

---

# 8. Acceptance Criteria

### AC-001

Searching by hotel name returns matching hotels.

### AC-002

Searching by location returns matching hotels.

### AC-003

Only active hotels appear in search results.

### AC-004

Pagination returns the correct page of results.

### AC-005

Filtering by rating returns only qualified hotels.

### AC-006

Sorting by price displays hotels in the correct order.

### AC-007

Booked rooms are excluded from room availability.

### AC-008

Locked rooms are excluded from room availability.

### AC-009

Inactive rooms are not displayed.

### AC-010

Search results contain hotel name, location, rating, and starting price.

---

# 9. Out of Scope

The Search & Discovery feature does **not** include:

- User registration
- User authentication
- Hotel management
- Room management
- Booking creation
- Booking cancellation
- Payment processing
- Review and rating submission
- Email notification
- Voucher or promotion management