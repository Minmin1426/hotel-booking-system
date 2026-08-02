# Implementation Plan: 003-booking-management

**Branch**: `main` / `003-booking-management` | **Date**: 2026-06-23 | **Last Updated**: 2026-07-28  
**Spec**: [spec.md](file:///c:/Users/Minmin/Documents/GitHub/hotel-booking-system/specs/003-booking-management/spec.md)

---

## 1. Executive Summary
Implement and expand the core **Booking Management Subsystem** covering:
- Individual & Group Booking (>5 rooms with 25% discount).
- Room Lock engine with dynamic timeout configuration (10 to 30 mins) and active lock renewal endpoint.
- Block / Batch Booking engine with Excel (.xlsx) file upload, validation parsing, and Admin approval workflow.
- Automated background lock release via `@Scheduled` `RoomLockCleanupScheduler`.
- 30% Deposit & 100% Full Payment options.
- Corporate Tax Profile (CTP) VAT invoice support.
- Meal Ticket QR code generation & restaurant scanning.
- Automated Cancellation & E-Wallet Refund Engine.
- Administrative & Receptionist CRUD booking control suite.

---

## 2. Technical Context
- **Language / Runtime**: Java 17 (JDK 18 execution wrapper)
- **Framework**: Spring Boot 4.0.0-M1, Spring Data JPA, Spring Security
- **Database**: PostgreSQL (Neon Cloud DB) & SQL Server
- **Frontend Stack**: React 18, Vite 8, TailwindCSS
- **Testing**: JUnit 5, Mockito, Spring Security Test (207 tests passed)

---

## 3. Architecture & Component Mapping

```
src/main/java/com/hotelbooking/
├── booking/
│   ├── BookingController.java        # REST Endpoints for booking CRUD, date validation, lock renew, group deposits
│   ├── AdminBookingController.java   # Admin & Receptionist endpoints for booking status & overrides
│   ├── BookingService.java           # Service contract
│   ├── BookingServiceImpl.java       # Core business logic: group discounts, room verification, status handling
│   ├── BookingRepository.java        # JPA Repository with custom queries & pessimistic locking
│   ├── BookingRoom.java              # Booking-Room junction entity
│   ├── BookingRoomRepository.java    # Junction repository
│   └── batch/
│       ├── BatchBookingController.java       # User endpoint for Block Booking Excel uploads
│       ├── AdminBatchBookingController.java  # Admin approval & rejection of block requests
│       ├── BatchBookingService.java          # Batch service interface
│       ├── BatchBookingServiceImpl.java      # Batch processing, row validation & automated creation
│       ├── ExcelParsingService.java          # Apache POI Excel parsing service (.xlsx)
│       ├── BlockBookingRequest.java          # Entity for batch request headers
│       ├── BlockBookingRow.java              # Entity for batch request row items
│       ├── BlockBookingRequestRepository.java# Repository for batch requests
│       └── BlockBookingRowRepository.java    # Repository for batch rows
├── room/
│   ├── RoomLock.java                 # Temporary room locking entity
│   ├── RoomLockRepository.java       # Lock query repository
│   ├── RoomLockService.java          # Service interface
│   ├── RoomLockServiceImpl.java      # Room locking & availability checks
│   └── RoomLockCleanupScheduler.java # 60-second cron task releasing expired locks
└── setting/
    ├── SettingsController.java       # Admin controller to adjust lock timeout
    ├── SystemSettingService.java     # System configuration service
    └── SystemSettingServiceImpl.java # Dynamic lock duration lookup
```

---

## 4. Frontend Integration (50-Screen System Alignment)

- **`HotelsPage.jsx`**: Hero Search tabs for Individual vs Group Booking (>5 Rooms).
- **`HotelDetailPage.jsx`**: Interactive Group Calculator, 25% Discount calculation, 30% Deposit options, Group Member Manifest, and Excel (.xlsx) Import Widget.
- **`ProfilePage.jsx`**: My Bookings, Cancellation Request Modal, E-Wallet Top-up & Refund credit, Meal Tickets QR Vault, CTP Tax Form.
- **`StaffRoomPage.jsx`**: Receptionist express group check-in, room matrix, and QR code scanner.
- **`AdminDashboardPage.jsx`**: Booking management table, dynamic lock duration setting (10-30m), CTP approvals, Block Booking approval panel, and meal ticket scan audits.

---

## 5. Verification & Quality Assurance Plan

1. **Automated Unit Tests**:
   - `mvn test` (Target: 207 tests passed).
2. **Frontend Production Build**:
   - `npm run build` in `frontend/` (`built in 1.79s`).
3. **Integration Verification**:
   - Verify room lock expiration, lock renewal, block booking Excel parsing, group discount calculations, and CTP invoice flow.
