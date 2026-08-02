# Implementation Plan: 012-customer-wallet

**Branch**: `012-customer-wallet` | **Date**: 2026-07-23 | **Spec**: [spec.md](spec.md)

## Summary
Implement digital wallet system for personal and group (corporate) wallets. Support deposits, payments, refunds, full transaction history. Wallets are created automatically on user registration; group wallets are created on group creation.

## Technical Context
- **Language/Version**: Java 17
- **Framework**: Spring Boot 3.3.0, Spring Data JPA
- **Testing**: JUnit 5, Mockito
- **Database**: PostgreSQL via Flyway — `V25__Customer_wallet.sql`

## Constitution Check
- **Feature Package**: `com.hotelbooking.wallet` — new package
- **DTOs**: `WalletResponse`, `WalletBalanceResponse`, `WalletTransactionResponse`, `DepositRequest`, `PayBookingRequest`, `WalletAdjustmentRequest`
- **No business logic in controllers**: Wallet operations and balance arithmetic in `WalletServiceImpl`

## Project Structure

### Database Migration
- `src/main/resources/db/migration/postgresql/V25__Customer_wallet.sql`
  - Create `groups` table (corporate groups)
  - Create `group_memberships` table (links users to groups)
  - Create `wallets` table
  - Create `wallet_transactions` table
  - Add unique constraint on `(owner_user_id, wallet_type, group_id)` for wallets

### Source Code

#### Entities
- `src/main/java/com/hotelbooking/wallet/Wallet.java`
- `src/main/java/com/hotelbooking/wallet/WalletRepository.java`
- `src/main/java/com/hotelbooking/wallet/WalletTransaction.java`
- `src/main/java/com/hotelbooking/wallet/WalletTransactionRepository.java`
- `src/main/java/com/hotelbooking/wallet/Group.java`
- `src/main/java/com/hotelbooking/wallet/GroupRepository.java`
- `src/main/java/com/hotelbooking/wallet/GroupMembership.java`
- `src/main/java/com/hotelbooking/wallet/GroupMembershipRepository.java`

#### Services
- `src/main/java/com/hotelbooking/wallet/WalletService.java` — Interface
- `src/main/java/com/hotelbooking/wallet/WalletServiceImpl.java` — Create wallet on user registration, deposit, pay, refund, history, freeze/unfreeze
- `src/main/java/com/hotelbooking/user/UserServiceImpl.java` — Hook: on user creation, create PERSONAL wallet

#### DTOs
- `src/main/java/com/hotelbooking/wallet/dto/WalletResponse.java`
- `src/main/java/com/hotelbooking/wallet/dto/WalletTransactionResponse.java`
- `src/main/java/com/hotelbooking/wallet/dto/DepositRequest.java`
- `src/main/java/com/hotelbooking/wallet/dto/PayBookingRequest.java`
- `src/main/java/com/hotelbooking/wallet/dto/WalletAdjustmentRequest.java`
- `src/main/java/com/hotelbooking/wallet/dto/FreezeWalletRequest.java`

#### Controllers
- `src/main/java/com/hotelbooking/wallet/WalletController.java` — Customer endpoints: view wallets, balance, history, deposit, pay
- `src/main/java/com/hotelbooking/wallet/AdminWalletController.java` — Admin: list, freeze, adjust

### Testing
- `src/test/java/com/hotelbooking/wallet/WalletServiceTest.java` — Unit tests: wallet auto-creation, deposit, pay, refund, freeze, history
- `src/test/java/com/hotelbooking/wallet/WalletControllerTest.java` — Controller tests

## Non-Functional Requirements
- **Atomicity**: Wallet balance updates must use pessimistic locking (`@Lock(PESSIMISTIC_WRITE)`) to prevent race conditions on concurrent payments
- **Audit**: Every transaction recorded with before/after balance
- **Security**: Wallet owner only — non-owners get 403
- **Performance**: Indexed on `wallet_id`, `user_id`, `created_at`
