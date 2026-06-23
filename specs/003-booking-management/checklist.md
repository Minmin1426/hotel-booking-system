# Validation Checklist: 003-booking-management

**Purpose**: Verifies stays validation, transactions, room locks, and cleanup schedulers.
**Created**: 2026-06-23
**Feature**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)

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
