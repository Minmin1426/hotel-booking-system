# Feature Specification: 005-inventory-catalog

**Feature Branch**: `005-inventory-catalog`
**Created**: 2026-06-23
**Status**: Completed

## User Scenarios & Testing

### User Story 1 - Add/Edit Hotel (Priority: P1)
As an Admin, I want to add new hotels and edit existing hotel details, so I can display the catalog accurately.

**Why this priority**: Required to populate the database with catalog entries.
**Independent Test**: Register a hotel via POST `/api/v1/hotels` and verify the created record can be queried.

**Acceptance Scenarios**:
1. **Given** an admin, **When** submitting valid hotel data, **Then** the hotel is created and returns HTTP 201 Created.
2. **Given** an admin, **When** updating an existing hotel, **Then** changes are stored in the database.

### User Story 2 - Delete Hotel (Priority: P2)
As an Admin, I want to delete a hotel so it no longer appears in search listings.

**Why this priority**: Maintain inventory correctness.
**Independent Test**: Soft-delete a hotel and verify it doesn't appear in client search endpoints, but exists in the DB.

**Acceptance Scenarios**:
1. **Given** an active hotel, **When** deleting the hotel, **Then** the system updates its `isActive` status to false (soft-delete).
2. **Given** a hotel with active bookings, **When** deleting the hotel, **Then** the system rejects deletion to prevent breaking active reservations.

### User Story 3 - Room Inventory & Hotel Image Management (Priority: P2)
As an Admin, I want to add/edit rooms and upload hotel images, to provide detailed information to users.

**Why this priority**: Crucial for search results detail pages and pricing calculations.
**Independent Test**: Add rooms to hotel ID 1 and upload an image, checking availability.

**Acceptance Scenarios**:
1. **Given** a hotel, **When** adding a room, **Then** the room is saved and mapped to that hotel ID.
2. **Given** an image file (e.g. JPG, PNG), **When** uploading to a hotel, **Then** the file path is saved and returned.

## Requirements

### Functional Requirements
- **FR-001**: Hotel deletions must be soft-deletions to maintain historical booking records.
- **FR-002**: Database constraints must prevent deleting a hotel or room if it has active bookings.
- **FR-003**: Image uploads must restrict format to `jpg`, `png`, and `webp` only.

### Key Entities
- **Hotel**: Represents hotel details.
- **Room**: Represents room details and pricing.
- **HotelImage**: Represents catalog image URLs.

## Success Criteria
- **SC-001**: Proper RBAC enforcement (only ADMIN/STAFF can edit/delete inventory).
- **SC-002**: Zero database orphans or orphaned booking records.
