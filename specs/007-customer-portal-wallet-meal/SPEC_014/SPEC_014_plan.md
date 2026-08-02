# Implementation Plan: 014-meal-ticket-wallet

**Branch**: `014-meal-ticket-wallet` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Digital meal ticket system: tickets issued on booking/tier benefit/manual action. Each ticket has a unique signed QR code. Staff scans QR to mark consumed. Daily job expires old tickets.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA, ZXing for QR generation
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V27__Meal_ticket_wallet.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.mealticket` — new package
- **DTOs**: `MealTicketResponse`, `ScanTicketRequest`, `IssueMealTicketRequest`, `BulkIssueRequest`, `MealTicketTypeResponse`
- **No business logic in controllers**: QR generation, scan validation, expiry in `MealTicketServiceImpl`
- **Security**: HMAC-SHA256 signature on QR prevents forgery

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V27__Meal_ticket_wallet.sql`
  - Create `meal_ticket_types` table — seed default types
  - Create `meal_tickets` table with unique constraint on `qr_code`
  - Create `meal_ticket_audit_log` table
  - Index on `meal_tickets.user_id`, `meal_tickets.status`, `meal_tickets.expires_at`

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/mealticket/MealTicketType.java`
- `src/main/java/com/hotelbooking/mealticket/MealTicketTypeRepository.java`
- `src/main/java/com/hotelbooking/mealticket/MealTicket.java`
- `src/main/java/com/hotelbooking/mealticket/MealTicketRepository.java`
- `src/main/java/com/hotelbooking/mealticket/MealTicketAuditLog.java`
- `src/main/java/com/hotelbooking/mealticket/MealTicketAuditLogRepository.java`

#### Services
- `src/main/java/com/hotelbooking/mealticket/MealTicketService.java` — Interface
- `src/main/java/com/hotelbooking/mealticket/MealTicketServiceImpl.java` — Issue tickets (auto on booking, manual by reception, bulk by GroupOwner), QR generation, scan/consume, expire
- `src/main/java/com/hotelbooking/mealticket/QrCodeGenerator.java` — Utility for HMAC-signed QR string + base64 PNG
- `src/main/java/com/hotelbooking/mealticket/MealTicketExpiryScheduler.java` — Daily expiry job
- `src/main/java/com/hotelbooking/booking/BookingServiceImpl.java` — Hook: on booking confirmation, issue included meal tickets
- `src/main/java/com/hotelbooking/loyalty/LoyaltyServiceImpl.java` — Hook: on tier upgrade, issue tier benefit tickets

#### DTOs
- `src/main/java/com/hotelbooking/mealticket/dto/MealTicketResponse.java`
- `src/main/java/com/hotelbooking/mealticket/dto/ScanTicketRequest.java`
- `src/main/java/com/hotelbooking/mealticket/dto/IssueMealTicketRequest.java`
- `src/main/java/com/hotelbooking/mealticket/dto/BulkIssueRequest.java`
- `src/main/java/com/hotelbooking/mealticket/dto/MealTicketTypeResponse.java`

#### Controllers
- `src/main/java/com/hotelbooking/mealticket/MealTicketController.java` — Customer: list my tickets, get QR image
- `src/main/java/com/hotelbooking/mealticket/RestaurantScanController.java` — Staff: `POST /restaurant/scan-ticket`
- `src/main/java/com/hotelbooking/mealticket/AdminMealTicketController.java` — Admin: types CRUD, manual issue, bulk issue

### Testing
- `src/test/java/com/hotelbooking/mealticket/MealTicketServiceTest.java` — Issue (auto/manual/bulk), QR generation, scan consume, expired/already-used rejection
- `src/test/java/com/hotelbooking/mealticket/QrCodeGeneratorTest.java` — HMAC signature verification
- `src/test/java/com/hotelbooking/mealticket/RestaurantScanControllerTest.java` — Scan endpoint with valid/invalid QR

## Non-Functional Requirements
- **Security**: HMAC-SHA256 signature prevents QR forgery; signature verified on every scan
- **Performance**: Indexed on status and expiry for fast wallet listing and expiry job
- **Audit**: Every action (issue, scan, expire, cancel) recorded in audit log
- **Reliability**: Atomic scan operation — ticket state update + audit log in same transaction
