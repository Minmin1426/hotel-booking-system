# Validation Checklist: 003-booking-management

**Purpose**: Verifies stay date validations, transactions, room locks, lock renewal, cleanup schedulers, group discounts, 30% deposits, CTP tax profiles, block booking Excel processing, refund policies, admin CRUD, error handling, security, and performance.
**Last Updated**: 2026-07-28  
**Feature Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)

---

## Stay Date Validation
- [x] CHK001 Verify stay date validation rejects past check-in dates with standard 400 Bad Request response.
- [x] CHK002 Verify checkout date before check-in date is rejected with standard 400 Bad Request response.
- [x] CHK003 Verify check-in date equal to check-out date is rejected.

---

## Booking & Transaction Controls
- [x] CHK004 Verify booking creation updates room lock record under atomic database transaction.
- [x] CHK005 Verify double booking same room for overlapping dates is rejected under concurrent thread testing.
- [x] CHK006 Verify check-out date calculations map to correct total booking price based on base room price.
- [x] CHK007 Verify target room IDs belong strictly to the requested hotel ID in `BookingServiceImpl.createBooking`.
- [x] CHK008 Verify active room locks can be renewed via `PUT /api/v1/bookings/{id}/lock/renew`.

---

## Group Booking (>5 Rooms), 30% Deposit & CTP
- [x] CHK009 Verify 25% discount is applied automatically when booking quantity is >= 5 rooms.
- [x] CHK010 Verify 30% deposit calculation matches exact monetary breakdown and sets status to `DEPOSIT_30_PAID`.
- [x] CHK011 Verify Corporate Tax Profile (CTP) accepts valid MST Tax Codes and maps to VAT invoice generation.
- [x] CHK012 Verify Group Member Manifest allows interactive table entry and Excel (.xlsx) file import simulation.

---

## Block / Batch Booking Engine via Excel (.xlsx)
- [x] CHK013 Verify user can upload `.xlsx` manifest to `POST /api/v1/users/me/block-bookings`.
- [x] CHK014 Verify `ExcelParsingService` correctly parses rows into `BlockBookingRequest` and `BlockBookingRow` entities.
- [x] CHK015 Verify Admin can retrieve, approve (`POST /api/v1/admin/block-bookings/{id}/approve`), or reject block requests.
- [x] CHK016 Verify approval triggers automated creation of individual `Booking` records for all valid rows.

---

## Room Lock Scheduler & Dynamic Timeout
- [x] CHK017 Verify scheduler fires and deletes expired locks based on configured setting minutes (10 to 30 mins).
- [x] CHK018 Verify lock cleanup releases the room and updates corresponding pending booking status to `FAILED`.
- [x] CHK019 Verify active booking payments turn room locks to inactive/completed state.
- [x] CHK020 Verify Admin can update auto-release lock duration dynamically via `AdminDashboardPage.jsx?tab=bookings`.

---

## Cancellation & Refund Engine
- [x] CHK021 Verify cancellation request calculates correct refund percentage (100%, 80%, 50%, 0%) based on hours remaining until check-in.
- [x] CHK022 Verify refund amounts are credited back to Customer E-Wallet balance.

---

## Security & Non-Functional Verification
- [x] CHK023 Verify all `/api/v1/bookings` endpoints (except `/api/v1/bookings/validate-dates`) reject requests without a valid JWT token (HTTP 401/403).
- [x] CHK024 Verify response times for booking status retrieval and booking requests are under 300ms on average under load.
- [x] CHK025 Verify exception handling responses match the standardized JSON structure defined in `GlobalExceptionHandler`.
