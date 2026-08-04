# Implementation Plan: 008-operations-group-allocation

**Feature Branch:** `008-operations-group-allocation`  
**Created:** 2026-07-26  
**Status:** APPROVED | COMPLETED  
**Spec Reference:** [spec.md](spec.md)  

---

## 1. Executive Summary

This feature implements the **Hotel Partner Operations & Group Allocation Engine** (`008-operations-group-allocation`). It empowers hotel partners and restaurant managers with operational dashboards, hotel registration workflows, room matrix inventory management, automated room allocation for group bookings, restaurant meal package QR verification, dynamic pricing calculation, partner cancellation approvals, and admin hotel approval workflows.

---

## 2. Technical Stack

| Component | Version / Technology | Purpose |
|-----------|----------------------|---------|
| Language | Java 17 | Core programming language |
| Framework | Spring Boot 3.x | Application framework |
| Security | Spring Security | Role-Based Access Control (RBAC) for Partner/Admin/Staff |
| Persistence | Spring Data JPA + Hibernate | Database ORM |
| Database | SQL Server | Relational database storage |
| Migration | Flyway | Database version control |
| QR Code | ZXing (`com.google.zxing`) | QR Code generation and validation for meal tickets |
| Testing | JUnit 5 + Mockito | Unit and Integration testing |

---

## 3. Architecture & Package Structure

The implementation will be modularized across the core package structure (`com.hotelbooking.*`):

```
src/main/java/com/hotelbooking/
├── report/
│   ├── PartnerDashboardController.java   # GET /api/v1/partner/dashboard
│   └── service/PartnerDashboardService.java
├── hotel/
│   ├── HotelRegistrationController.java # POST /api/v1/partner/hotels, PUT /api/v1/admin/hotels/{id}/approve
│   ├── RestaurantFacilityController.java # CRUD /api/v1/partner/hotels/{id}/restaurant
│   └── service/HotelApprovalService.java
├── room/
│   ├── RoomMatrixController.java         # GET/PUT /api/v1/partner/hotels/{id}/room-matrix
│   ├── GroupAllocationController.java    # POST /api/v1/partner/group-bookings/{id}/auto-allocate
│   ├── DynamicPricingController.java     # POST/GET /api/v1/partner/hotels/{id}/pricing-rules
│   └── service/GroupAllocationService.java
├── booking/
│   ├── PartnerCancellationController.java # PUT /api/v1/partner/cancellations/{id}/approve
│   └── service/PartnerCancellationService.java
└── voucher/
    ├── MealPackageController.java        # POST/GET /api/v1/restaurant/meal-packages
    ├── MealTicketScanController.java     # POST /api/v1/restaurant/tickets/scan
    └── service/MealTicketScanService.java
```

---

## 4. Database Schema & Flyway Migrations

A new Flyway migration script `V23__Operations_Group_Allocation.sql` will be added under `src/main/resources/db/migration/sqlserver/`:

### 4.1 Database Tables
1. **`restaurant_facilities`**:
   - `id` (BIGINT PK), `hotel_id` (BIGINT FK), `name`, `capacity`, `opening_hours`, `status`
2. **`room_matrix_grid`**:
   - `id` (BIGINT PK), `hotel_id` (BIGINT FK), `room_id` (BIGINT FK), `date` (DATE), `status` (CLEAN|DIRTY|MAINTENANCE|OCCUPIED), `locked_for_allocation` (BIT)
3. **`pricing_rules`**:
   - `id` (BIGINT PK), `hotel_id` (BIGINT FK), `room_type_id` (BIGINT FK), `rule_type` (WEEKEND_SURCHARGE|GROUP_DISCOUNT), `multiplier` (DECIMAL), `discount_percentage` (DECIMAL), `start_date`, `end_date`, `priority` (INT)
4. **`meal_packages` & `meal_tickets`**:
   - `id` (BIGINT PK), `hotel_id` (BIGINT FK), `package_name`, `price`, `description`
   - `ticket_id` (BIGINT PK), `booking_id` (BIGINT FK), `qr_code_hash` (VARCHAR unique), `status` (UNUSED|USED|EXPIRED), `scanned_at` (DATETIME2)

---

## 5. API Endpoints & DTOs

### 5.1 Hotel Dashboard
- `GET /api/v1/partner/hotels/{hotelId}/dashboard` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Returns `PartnerDashboardResponse`: Occupancy rate %, revenue summary, available rooms, upcoming group bookings list.

### 5.2 Hotel & Restaurant Registration
- `POST /api/v1/partner/hotels/register` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Submits hotel profile + restaurant info; sets status to `PENDING_APPROVAL`.
- `PUT /api/v1/admin/hotels/{hotelId}/approval` (`@PreAuthorize("hasRole('ADMIN')")`)
  - Admin approves (`APPROVED`) or rejects (`REJECTED`) hotel profile.

### 5.3 Room Matrix & Inventory
- `GET /api/v1/partner/hotels/{hotelId}/room-matrix` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Returns room status grid by date and room type.
- `PUT /api/v1/partner/rooms/{roomId}/status` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Updates room operational status (`CLEAN`, `DIRTY`, `MAINTENANCE`).

### 5.4 Group Room Allocation Engine
- `POST /api/v1/partner/group-bookings/{bookingId}/auto-allocate` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Executes auto-allocation algorithm for guests in group bookings using `@Transactional(isolation = Isolation.SERIALIZABLE)` to prevent race conditions.

### 5.5 Restaurant Meal Ticket Scanning
- `POST /api/v1/restaurant/tickets/scan` (`@PreAuthorize("hasAnyRole('RESTAURANT_STAFF', 'PARTNER')")`)
  - Scans QR payload, validates hash, updates status to `USED`, returns ticket details or rejects duplicate scans.

### 5.6 Dynamic Pricing Calculation
- `POST /api/v1/partner/hotels/{hotelId}/pricing-rules` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Defines weekend surcharge multipliers and group discount thresholds.
- `POST /api/v1/pricing/calculate`
  - Calculates net rate given dates, group size, and room type.

### 5.7 Cancellation Approvals
- `PUT /api/v1/partner/cancellations/{cancellationId}/approve` (`@PreAuthorize("hasRole('PARTNER')")`)
  - Approves pending cancellation, calculates refund according to policy, updates booking status to `CANCELLED`.

---

## 6. Business Logic & Edge Case Handling

1. **Auto Allocation Atomicity**:
   - Uses pessimistic locking (`SELECT ... WITH (UPDLOCK)`) on room availability records during allocation. If inventory is insufficient, rolls back completely.
2. **Duplicate QR Scan Guard**:
   - Atomic database update `UPDATE meal_tickets SET status = 'USED', scanned_at = NOW() WHERE qr_code_hash = :hash AND status = 'UNUSED'`. Affected row count = 0 indicates duplicate/invalid scan.
3. **Late Cancellation Check**:
   - If `cancellation_request_date >= checkin_date`, automatic approval is blocked and flagged for admin intervention.

---

## 7. Verification & Test Plan

### Automated Tests
- `GroupAllocationServiceTest`: Test group allocation with exact matching rooms, insufficient rooms rollback, concurrency race condition.
- `MealTicketScanServiceTest`: Test valid QR scan, duplicate scan rejection, expired ticket handling.
- `DynamicPricingServiceTest`: Test weekend surcharge calculation, group discount stacking, priority order.
- `PartnerDashboardServiceTest`: Test occupancy rate aggregation and RBAC permission checks.

### Manual Verification
- Verify Admin approval changes hotel status to `APPROVED` and displays hotel in public search results.
- Verify Room Matrix grid updates dynamically upon room status modification.
