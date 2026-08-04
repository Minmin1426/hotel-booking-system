# Tasks: 012-customer-wallet

## Phase 1: Database & Entities
- [x] T001 Create Flyway migration `V25__Customer_wallet.sql` — `groups`, `group_memberships`, `wallets`, `wallet_transactions` tables with constraints and indexes
- [x] T002 Implement `Group` entity and `GroupRepository`
- [x] T003 Implement `GroupMembership` entity and `GroupMembershipRepository`
- [x] T004 Implement `Wallet` entity and `WalletRepository` with `findByOwnerAndLockForUpdate()` using `@Lock(PESSIMISTIC_WRITE)`
- [x] T005 Implement `WalletTransaction` entity and `WalletTransactionRepository`

## Phase 2: Wallet Service
- [x] T006 Implement `WalletService.createPersonalWallet()` — auto-create PERSONAL wallet on user registration (hook in UserServiceImpl.register())
- [x] T007 Implement `WalletService.getMyWallets()` — return all wallets owned by current user
- [x] T008 Implement `WalletService.getWalletBalance()` — return single wallet balance with ownership check
- [x] T009 Implement `WalletService.getTransactionHistory()` — paginated history with optional type filter
- [x] T010 Implement `WalletService.deposit()` — initiate Stripe top-up (full Stripe flow in spec 013), record transaction with status PENDING
- [x] T011 Implement `WalletService.confirmDeposit()` — Stripe webhook callback credits wallet, updates transaction to SUCCESS
- [x] T012 Implement `WalletService.payBooking()` — atomic balance check + deduction + transaction record + booking status update
- [x] T013 Implement `WalletService.refundToWallet()` — credit wallet on booking refund
- [x] T014 Implement `WalletService.freezeWallet()` / `unfreezeWallet()` / `closeWallet()` — admin operations

## Phase 3: Admin Operations
- [x] T015 Implement `WalletService.listAllWallets()` — admin paginated list with filters
- [x] T016 Implement `WalletService.manualAdjustment()` — admin credit/debit with audit
- [x] T017 Implement `WalletService.changeWalletStatus()` — admin status change

## Phase 4: Controllers
- [x] T018 Create `WalletController` — `GET /users/me/wallets`, `GET /users/me/wallets/{id}/balance`, `GET /users/me/wallets/{id}/transactions`, `POST /users/me/wallets/{id}/deposit`, `POST /users/me/wallets/pay-booking`
- [x] T019 Create `AdminWalletController` — `GET /admin/wallets`, `PATCH /admin/wallets/{id}/status`, `POST /admin/wallets/{id}/adjust`

## Phase 5: UserService Hook
- [x] T020 Update `UserServiceImpl.register()` — after creating user, call `WalletService.createPersonalWallet(user)`
- [x] T021 Update `AuthServiceImpl.register()` (in spec 007) — same hook

## Phase 6: Error Handling
- [x] T022 Add `InsufficientBalanceException`, `WalletFrozenException`, `WalletNotFoundException` to GlobalExceptionHandler
- [x] T023 Handle `INVALID_AMOUNT` (deposit <= 0) → 400
- [x] T024 Ownership check on every endpoint — non-owner → 403

## Phase 7: Testing
- [x] T025 Write `WalletServiceTest.java` — wallet auto-create, deposit, pay, refund, freeze, manual adjustment, history
- [x] T026 Write `WalletControllerTest.java` — controller tests: unauthorized → 401, non-owner → 403, deposit → 200, pay → 200
- [x] T027 Verify all new acceptance criteria (AC-046 to AC-052) are covered
