# Tasks: 014-meal-ticket-wallet

## Phase 1: Database & Entities
- [x] T001 Create Flyway migration `V27__Meal_ticket_wallet.sql` — `meal_ticket_types`, `meal_tickets`, `meal_ticket_audit_log` tables; seed default types
- [x] T002 Implement `MealTicketType` entity and repository
- [x] T003 Implement `MealTicket` entity and repository (with `findByQrCode()`, `findByUserIdAndStatus()`)
- [x] T004 Implement `MealTicketAuditLog` entity and repository

## Phase 2: QR Code Generation
- [x] T005 Implement `QrCodeGenerator.generateQr()` — produces HMAC-SHA256-signed string + base64 PNG using ZXing
- [x] T006 Implement `QrCodeGenerator.verifyQr()` — verifies signature and extracts payload

## Phase 3: Meal Ticket Service
- [x] T007 Implement `MealTicketService.issueTicket()` — generate QR, save ticket, record audit log
- [x] T008 Implement `MealTicketService.issueManualTicket()` — receptionist action with notes
- [x] T009 Implement `MealTicketService.issueBulkTickets()` — GroupOwner issues to multiple members
- [x] T010 Implement `MealTicketService.scanAndConsume()` — verify QR signature, check status, mark USED, record staff ID, audit log
- [x] T011 Implement `MealTicketService.getMyTickets()` — paginated with status/type filters
- [x] T012 Implement `MealTicketService.getQrImage()` — return base64 PNG for ticket
- [x] T013 Implement `MealTicketService.expireOldTickets()` — bulk update where expiresAt < now

## Phase 4: Booking Integration Hook
- [x] T014 Update `BookingServiceImpl.confirmBooking()` — after confirmation, parse booking inclusions, issue meal tickets
- [x] T015 Update `LoyaltyServiceImpl.evaluateTier()` — on promotion, issue tier benefit tickets (e.g., PLATINUM free breakfast)

## Phase 5: Scheduled Job
- [x] T016 Implement `MealTicketExpiryScheduler.expireOldTickets()` — `@Scheduled(cron = "0 0 1 * * *")` runs at 1 AM daily

## Phase 6: Controllers
- [x] T017 Create `MealTicketController` — `GET /users/me/meal-tickets`, `GET /users/me/meal-tickets/{id}/qr`
- [x] T018 Create `RestaurantScanController` — `POST /restaurant/scan-ticket`
- [x] T019 Create `AdminMealTicketController` — `GET /admin/meal-ticket-types`, `POST /admin/meal-ticket-types`, `PUT /admin/meal-ticket-types/{id}`, `POST /admin/meal-tickets/issue`, `POST /groups/{groupId}/meal-tickets/issue`

## Phase 7: Error Handling
- [x] T020 Add `InvalidQrCodeException`, `TicketAlreadyUsedException`, `TicketExpiredException`, `TicketNotFoundException` to GlobalExceptionHandler

## Phase 8: Testing
- [x] T021 Write `QrCodeGeneratorTest.java` — generate + verify round-trip, tampered QR rejected
- [x] T022 Write `MealTicketServiceTest.java` — issue (auto/manual/bulk), scan consume, expired/already-used rejection, expiry job
- [x] T023 Write `RestaurantScanControllerTest.java` — valid scan → 200, invalid QR → 401, expired → 400
- [x] T024 Verify all new acceptance criteria (AC-061 to AC-067) are covered
