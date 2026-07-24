# Implementation Plan: 011-loyalty-membership-tiers

**Branch**: `011-loyalty-membership-tiers` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Implement a tiered loyalty program with 8 tiers (4 individual + 4 corporate). Auto-evaluate tier after each successful payment, award points with multipliers, track full history, allow admin manual adjustments.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V24__Loyalty_membership_tiers.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.loyalty` — new package
- **Existing Dependencies**: Reuses `User`, `Booking`, `Payment` entities; hooks into payment SUCCESS flow
- **DTOs**: `TierInfoResponse`, `TierHistoryResponse`, `TierDefinitionResponse`, `AdjustTierRequest`
- **No business logic in controllers**: All tier evaluation in `LoyaltyServiceImpl`
- **Scheduled Job**: Daily job to demote expired tier-eligible users (rolling 12-month spend drops)

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V24__Loyalty_membership_tiers.sql`
  - Add `current_tier` and `tier_evaluated_at` to `users`
  - Create `tier_definitions` table — seed 8 default tiers
  - Create `loyalty_point_ledger` table
  - Create `tier_history` table
  - Index on `loyalty_point_ledger.user_id`, `tier_history.user_id`

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/loyalty/TierDefinition.java`
- `src/main/java/com/hotelbooking/loyalty/TierDefinitionRepository.java`
- `src/main/java/com/hotelbooking/loyalty/LoyaltyPointLedger.java`
- `src/main/java/com/hotelbooking/loyalty/LoyaltyPointLedgerRepository.java`
- `src/main/java/com/hotelbooking/loyalty/TierHistory.java`
- `src/main/java/com/hotelbooking/loyalty/TierHistoryRepository.java`
- Extend `src/main/java/com/hotelbooking/user/User.java` with `currentTier`, `tierEvaluatedAt`

#### Services
- `src/main/java/com/hotelbooking/loyalty/LoyaltyService.java` — Interface
- `src/main/java/com/hotelbooking/loyalty/LoyaltyServiceImpl.java` — Award points, evaluate tier, get tier info
- `src/main/java/com/hotelbooking/loyalty/TierEvaluationScheduler.java` — Daily scheduler for demotion of expired-window users
- `src/main/java/com/hotelbooking/payment/PaymentServiceImpl.java` — Hook: on payment SUCCESS, call LoyaltyService.evaluate()

#### DTOs
- `src/main/java/com/hotelbooking/loyalty/dto/TierInfoResponse.java`
- `src/main/java/com/hotelbooking/loyalty/dto/TierHistoryResponse.java`
- `src/main/java/com/hotelbooking/loyalty/dto/TierDefinitionResponse.java`
- `src/main/java/com/hotelbooking/loyalty/dto/AdjustTierRequest.java`

#### Controllers
- `src/main/java/com/hotelbooking/loyalty/LoyaltyController.java` — `GET /users/me/tier`, `GET /users/me/tier/history`
- `src/main/java/com/hotelbooking/loyalty/AdminLoyaltyController.java` — Tier definitions CRUD, manual adjust, points ledger

### Testing
- `src/test/java/com/hotelbooking/loyalty/LoyaltyServiceTest.java` — Unit tests: points calc, auto-promote, auto-demote, manual adjust, history recording
- `src/test/java/com/hotelbooking/loyalty/LoyaltyControllerTest.java` — Controller tests
