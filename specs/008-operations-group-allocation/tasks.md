# Tasks: 008-operations-group-allocation

**Input**: Design documents from `/specs/008-operations-group-allocation/`

## Phase 1: Database & Flyway Migrations
- [ ] T001 Create Flyway migration script `V23__Operations_Group_Allocation.sql` — create `restaurant_facilities`, `room_matrix_grid`, `pricing_rules`, `meal_packages`, and `meal_tickets` tables with indexes.
- [ ] T002 Implement `RestaurantFacility` JPA entity under `com.hotelbooking.hotel`.
- [ ] T003 Implement `RoomMatrixGrid` JPA entity under `com.hotelbooking.room`.
- [ ] T004 Implement `PricingRule` JPA entity under `com.hotelbooking.room`.
- [ ] T005 Implement `MealPackage` and `MealTicket` JPA entities under `com.hotelbooking.voucher`.
- [ ] T006 Implement Spring Data JPA repositories: `RestaurantFacilityRepository`, `RoomMatrixGridRepository`, `PricingRuleRepository`, `MealTicketRepository`.

## Phase 2: Hotel Dashboard & Partner Registration (US1, US2, US8)
- [ ] T007 Implement `PartnerDashboardServiceImpl.getDashboardStats()` — aggregate occupancy rate, revenue summary, room availability, and group bookings.
- [ ] T008 Expose `GET /api/v1/partner/hotels/{hotelId}/dashboard` in `PartnerDashboardController` secured with `@PreAuthorize("hasRole('PARTNER')")`.
- [ ] T009 Implement `HotelRegistrationServiceImpl.registerHotel()` — save hotel info & restaurant facility with status `PENDING_APPROVAL`.
- [ ] T010 Implement `HotelApprovalServiceImpl.approveOrRejectHotel()` for Admin approval workflow.
- [ ] T011 Expose `PUT /api/v1/admin/hotels/{hotelId}/approval` in `HotelApprovalController` secured with `@PreAuthorize("hasRole('ADMIN')")`.

## Phase 3: Room Inventory & Matrix Management (US3)
- [ ] T012 Implement `RoomMatrixServiceImpl.getRoomMatrix()` — compile daily matrix grid by room type and date range.
- [ ] T013 Implement `RoomMatrixServiceImpl.updateRoomStatus()` — update room operational status (`CLEAN`, `DIRTY`, `MAINTENANCE`).
- [ ] T014 Expose `GET/PUT /api/v1/partner/hotels/{hotelId}/room-matrix` endpoints in `RoomMatrixController`.

## Phase 4: Group Room Allocation Engine (US4)
- [ ] T015 Implement `GroupAllocationServiceImpl.autoAllocateRooms()` — query available clean rooms matching group requirements with pessimistic locking.
- [ ] T016 Implement room capacity and duplicate assignment validation logic with atomic rollback on failure.
- [ ] T017 Expose `POST /api/v1/partner/group-bookings/{bookingId}/auto-allocate` in `GroupAllocationController`.

## Phase 5: Restaurant Meal Package & QR Ticket Scanning (US5)
- [ ] T018 Implement `MealPackageServiceImpl.createPackage()` — CRUD operations for meal packages.
- [ ] T019 Implement QR code generator helper `QRCodeUtils` using ZXing library for meal tickets.
- [ ] T020 Implement `MealTicketScanServiceImpl.scanTicket()` — atomic update to set ticket status `USED` and reject duplicate scans.
- [ ] T021 Expose `POST /api/v1/restaurant/tickets/scan` in `MealTicketScanController` secured with `@PreAuthorize("hasAnyRole('RESTAURANT_STAFF', 'PARTNER')")`.

## Phase 6: Dynamic Pricing Engine & Group Discount (US6)
- [ ] T022 Implement `DynamicPricingServiceImpl.calculatePrice()` — evaluate weekend surcharges and group volume discounts according to rule priority.
- [ ] T023 Expose `POST /api/v1/partner/hotels/{hotelId}/pricing-rules` and `POST /api/v1/pricing/calculate`.

## Phase 7: Partner Cancellation Approval Workflow (US7)
- [ ] T024 Implement `PartnerCancellationServiceImpl.approveCancellation()` — review pending request, verify check-in date criteria, update booking status to `CANCELLED`, and trigger refund engine.
- [ ] T025 Expose `PUT /api/v1/partner/cancellations/{cancellationId}/approve` in `PartnerCancellationController`.

## Phase 8: Unit & Integration Testing
- [ ] T026 Write `GroupAllocationServiceTest.java` — test auto-allocation logic, capacity check, and concurrency locking.
- [ ] T027 Write `MealTicketScanServiceTest.java` — test valid QR ticket scan, duplicate scan rejection, and expired ticket.
- [ ] T028 Write `DynamicPricingServiceTest.java` — test weekend surcharge and group discount calculations.
- [ ] T029 Write `PartnerDashboardServiceTest.java` — test aggregation metrics and RBAC permissions.
