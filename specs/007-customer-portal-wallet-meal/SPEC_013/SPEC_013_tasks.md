# Tasks: 013-wallet-topup-spending-limits

## Phase 1: Database & Entities
- [x] T001 Create Flyway migration `V26__Wallet_topup_spending_limits.sql` — `topup_configs`, `topup_history`, `spending_limits`, `spending_limit_history`, `spending_tracking` tables
- [x] T002 Implement `TopUpConfig` entity and repository
- [x] T003 Implement `TopUpHistory` entity and repository
- [x] T004 Implement `SpendingLimit` entity and repository
- [x] T005 Implement `SpendingLimitHistory` entity and repository
- [x] T006 Implement `SpendingTracking` entity and repository (with daily/monthly aggregation queries)

## Phase 2: Top-Up Service
- [x] T007 Implement `TopUpService.configureAutoTopUp()` — upsert config for wallet
- [x] T008 Implement `TopUpService.initiateManualTopUp()` — Stripe Checkout Session creation, return URL
- [x] T009 Implement `TopUpService.handleStripeWebhook()` — verify webhook signature, credit wallet on `checkout.session.completed`, record history
- [x] T010 Implement `TopUpService.getTopUpHistory()` — paginated
- [x] T011 Implement `TopUpService.triggerAutoTopUpIfNeeded()` — checks balance vs threshold, calls Stripe charge
- [x] T012 Enforce `maxSingleTopUp` and `maxWalletBalance` checks before top-up

## Phase 3: Auto Top-Up Scheduler
- [x] T013 Implement `AutoTopUpScheduler.scanWallets()` — runs every 5 minutes, finds wallets with enabled auto-topup and balance < threshold
- [x] T014 Daily limit enforcement — count today's auto-topups, skip if >= 5

## Phase 4: Spending Limit Service
- [x] T015 Implement `SpendingLimitService.setMemberLimit()` — GroupOwner sets limit, record history
- [x] T016 Implement `SpendingLimitService.checkLimit()` — validates payment against PER_TRANSACTION, DAILY (rolling 24h), MONTHLY (current month)
- [x] T017 Implement `SpendingLimitService.getSpendingStatus()` — return limits, current spend, remaining

## Phase 5: Wallet Integration
- [x] T018 Update `WalletServiceImpl.payBooking()` — before deducting balance, call `SpendingLimitService.checkLimit()`
- [x] T019 On payment success, update `spending_tracking` (daily and monthly totals)

## Phase 6: Controllers
- [x] T020 Create `TopUpController` — `PUT /users/me/wallets/{id}/auto-topup`, `POST /users/me/wallets/{id}/topup`, `GET /users/me/wallets/{id}/topup-history`
- [x] T021 Create `SpendingLimitController` — `PUT /groups/{groupId}/members/{userId}/spending-limit`, `GET /users/me/spending-status`
- [x] T022 Create `AdminTopUpController` — `PUT /admin/topup-limits`
- [x] T023 Create `StripeWebhookController` — `POST /webhooks/stripe/topup` (extends existing payment webhook)

## Phase 7: Error Handling
- [x] T024 Add `SpendingLimitExceededException`, `TopUpLimitExceededException`, `WalletBalanceLimitException`, `AutoTopUpLimitReachedException` to GlobalExceptionHandler

## Phase 8: Testing
- [x] T025 Write `TopUpServiceTest.java` — manual top-up flow, auto top-up trigger, daily limit, max balance check
- [x] T026 Write `SpendingLimitServiceTest.java` — per-transaction, daily, monthly checks, boundary cases
- [x] T027 Write `StripeWebhookControllerTest.java` — webhook signature verification, credit flow
- [x] T028 Verify all new acceptance criteria (AC-053 to AC-060) are covered
