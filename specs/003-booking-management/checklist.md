# Validation Checklist: 003-booking-management

**Purpose**: Verifies stays validation, transactions, room locks, cleanup schedulers, error handling, security, and performance.
**Created**: 2026-06-23
**Feature**: [spec.md](file:///c:/Users/minhn/OneDrive/Ta%CC%80i%20li%C3%AA%CC%A3u/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)

## Stay Date Validation

- [ ] CHK001 Verify stay date validation rejects past check-in dates with standard 400 Bad Request response.
- [ ] CHK002 Verify checkout date before check-in date is rejected with standard 400 Bad Request response.
- [ ] CHK003 Verify check-in date equal to check-out date is rejected.

## Booking & Transaction Controls

- [ ] CHK004 Verify booking creation updates room lock record under atomic database transaction.
- [ ] CHK005 Verify double booking same room for overlapping dates is rejected under concurrent thread testing.
- [ ] CHK006 Verify check-out date calculations map to correct total booking price based on base room price.

## Room Lock Scheduler

- [ ] CHK007 Verify scheduler fires and deletes expired locks based on configured setting minutes.
- [ ] CHK008 Verify lock cleanup releases the room and updates corresponding pending booking status to `FAILED`.
- [ ] CHK009 Verify active booking payments turn room locks to inactive/completed state.

## Non-functional & Security Requirements

- [ ] CHK010 Verify all `/api/v1/bookings` endpoints (except room search and `/api/v1/bookings/validate-dates`) reject requests without a valid JWT token (HTTP 401/403).
- [ ] CHK011 Verify response times for booking status retrieval and booking requests are under 500ms on average under load.
- [ ] CHK012 Verify exception handling responses exactly match the standardized JSON structure defined in `GlobalExceptionHandler` (with fields `timestamp`, `status`, `error`, `message`, `path`).
