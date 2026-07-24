# Implementation Plan: 013-wallet-topup-spending-limits

**Branch**: `013-wallet-topup-spending-limits` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Add Stripe Checkout-based top-up, auto top-up configuration with thresholds, and per-member spending limits (per-transaction, daily, monthly). Stripe webhooks credit wallets; spending limits are enforced at payment time.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA, Stripe Java SDK 24.2.0 (existing)
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V26__Wallet_topup_spending_limits.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.wallet.topup` — extends `com.hotelbooking.wallet`
- **DTOs**: `TopUpConfigRequest`, `TopUpRequest`, `TopUpHistoryResponse`, `SpendingLimitRequest`, `SpendingStatusResponse`, `GlobalTopUpLimitsRequest`
- **No business logic in controllers**: Limit enforcement and Stripe interaction in `TopUpServiceImpl`

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V26__Wallet_topup_spending_limits.sql`
  - Create `topup_configs` table
  - Create `topup_history` table
  - Create `spending_limits` table
  - Create `spending_limit_history` table
  - Create `spending_tracking` table

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/wallet/topup/TopUpConfig.java`
- `src/main/java/com/hotelbooking/wallet/topup/TopUpConfigRepository.java`
- `src/main/java/com/hotelbooking/wallet/topup/TopUpHistory.java`
- `src/main/java/com/hotelbooking/wallet/topup/TopUpHistoryRepository.java`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimit.java`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimitRepository.java`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimitHistory.java`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimitHistoryRepository.java`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingTracking.java`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingTrackingRepository.java`

#### Services
- `src/main/java/com/hotelbooking/wallet/topup/TopUpService.java` — Interface
- `src/main/java/com/hotelbooking/wallet/topup/TopUpServiceImpl.java` — Stripe Checkout integration, auto top-up logic, limit enforcement
- `src/main/java/com/hotelbooking/wallet/topup/AutoTopUpScheduler.java` — Periodic check for wallets below threshold
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimitService.java` — Interface
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimitServiceImpl.java` — Set/check limits, validate payments against limits
- `src/main/java/com/hotelbooking/wallet/WalletServiceImpl.java` — Integrate spending limit check in `payBooking()`

#### DTOs
- `src/main/java/com/hotelbooking/wallet/topup/dto/TopUpConfigRequest.java`
- `src/main/java/com/hotelbooking/wallet/topup/dto/TopUpRequest.java`
- `src/main/java/com/hotelbooking/wallet/topup/dto/TopUpHistoryResponse.java`
- `src/main/java/com/hotelbooking/wallet/topup/dto/SpendingLimitRequest.java`
- `src/main/java/com/hotelbooking/wallet/topup/dto/SpendingLimitResponse.java`
- `src/main/java/com/hotelbooking/wallet/topup/dto/SpendingStatusResponse.java`
- `src/main/java/com/hotelbooking/wallet/topup/dto/GlobalTopUpLimitsRequest.java`

#### Controllers
- `src/main/java/com/hotelbooking/wallet/topup/TopUpController.java` — `PUT /wallets/{id}/auto-topup`, `POST /wallets/{id}/topup`, `GET /wallets/{id}/topup-history`
- `src/main/java/com/hotelbooking/wallet/topup/SpendingLimitController.java` — `PUT /groups/{groupId}/members/{userId}/spending-limit`, `GET /users/me/spending-status`
- `src/main/java/com/hotelbooking/wallet/topup/AdminTopUpController.java` — `PUT /admin/topup-limits`

#### Stripe Integration
- `src/main/java/com/hotelbooking/wallet/topup/StripeWebhookController.java` — Webhook endpoint for top-up success/failure

### Testing
- `src/test/java/com/hotelbooking/wallet/topup/TopUpServiceTest.java` — Stripe Checkout creation, auto top-up trigger, daily limit, max balance check
- `src/test/java/com/hotelbooking/wallet/topup/SpendingLimitServiceTest.java` — Per-transaction, daily, monthly limit checks
- `src/test/java/com/hotelbooking/wallet/topup/StripeWebhookControllerTest.java` — Webhook handling
