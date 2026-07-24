# Implementation Plan: 010-voucher-store-front

**Branch**: `010-voucher-store-front` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Build a customer-facing voucher marketplace (store) and an admin voucher campaign management panel. Customers can browse claimable vouchers, claim them to their personal wallet, and apply claimed vouchers at checkout. Admins can CRUD voucher campaigns with scheduling, account-type filtering, and usage limits. Includes voucher statistics and claim/redemption tracking.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V23__Voucher_store_front.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.voucher` — extends existing voucher package
- **Existing Dependencies**: Reuses existing `Voucher`, `VoucherRepository`; extends payment integration from spec 004
- **New Entity**: `UserVoucher` for per-user claim tracking
- **DTOs**: `VoucherStoreResponse`, `ClaimVoucherResponse`, `UserVoucherResponse`, `VoucherAdminRequest`, `VoucherStatsResponse`
- **No business logic in controllers**: All claim logic, validation, and statistics in `VoucherStoreServiceImpl`

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V23__Voucher_store_front.sql`
  - Add columns to existing `vouchers` table: `name`, `description`, `start_date`, `end_date`, `for_account_type`, `created_by`
  - Create `user_vouchers` table with unique constraint on `(user_id, voucher_id)`
  - Add FKs for `created_by` → `users.user_id`, `booking_id` → `bookings.booking_id`
  - Index on `user_vouchers.user_id`, `vouchers.for_account_type`

### Source Code

#### Extended Voucher Entity
- `src/main/java/com/hotelbooking/voucher/Voucher.java` — Add new fields

#### New Entities & Repositories
- `src/main/java/com/hotelbooking/voucher/UserVoucher.java`
- `src/main/java/com/hotelbooking/voucher/UserVoucherRepository.java`

#### Services
- `src/main/java/com/hotelbooking/voucher/VoucherStoreService.java` — Interface
- `src/main/java/com/hotelbooking/voucher/VoucherStoreServiceImpl.java` — Browse store, claim voucher, list wallet, voucher CRUD for admin, stats
- `src/main/java/com/hotelbooking/voucher/VoucherAdminService.java` — Admin-only operations (create, update, deactivate, list, stats)

#### DTOs
- `src/main/java/com/hotelbooking/voucher/dto/VoucherStoreResponse.java` — Public voucher view for store
- `src/main/java/com/hotelbooking/voucher/dto/ClaimVoucherResponse.java` — Claim confirmation
- `src/main/java/com/hotelbooking/voucher/dto/UserVoucherResponse.java` — Wallet item
- `src/main/java/com/hotelbooking/voucher/dto/VoucherAdminRequest.java` — Create/update request
- `src/main/java/com/hotelbooking/voucher/dto/VoucherStatsResponse.java` — Per-voucher statistics

#### Controllers
- `src/main/java/com/hotelbooking/voucher/VoucherStoreController.java` — Customer endpoints: browse, claim, wallet
- `src/main/java/com/hotelbooking/voucher/AdminVoucherController.java` — Admin CRUD and stats

#### Payment Integration
- Update `src/main/java/com/hotelbooking/payment/PaymentServiceImpl.java` — when applying voucher at checkout, verify voucher is in user's wallet (claim check)

### Testing
- `src/test/java/com/hotelbooking/voucher/VoucherStoreServiceTest.java` — Unit tests: browse (filtered by account type/date/usage), claim success, claim already-claimed, claim wrong account type, wallet list
- `src/test/java/com/hotelbooking/voucher/VoucherAdminServiceTest.java` — Admin CRUD, deactivation, stats
- `src/test/java/com/hotelbooking/voucher/VoucherStoreControllerTest.java` — Controller tests

## Non-Functional Requirements
- **Security**: Customer endpoints require authentication. Admin endpoints require ADMIN role. Unique constraint prevents double-claiming.
- **Performance**: Store browse query uses indexed filters. Wallet query indexed by `user_id`.
- **Data Integrity**: `currentUsage` increment and `UserVoucher.isUsed` update happen in the same transaction as payment confirmation.
- **Idempotency**: Claim endpoint is idempotent — re-calling on already-claimed voucher returns 400 `VOUCHER_ALREADY_CLAIMED` (no duplicate row due to unique constraint).
