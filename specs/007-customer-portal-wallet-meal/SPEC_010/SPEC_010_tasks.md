# Tasks: 010-voucher-store-front

**Input**: Design documents from `/specs/010-voucher-store-front/`

## Phase 1: Database & Entity Extensions
- [x] T001 Create Flyway migration `V23__Voucher_store_front.sql` — add columns to `vouchers` table, create `user_vouchers` table, add indexes and FKs
- [x] T002 Extend `Voucher.java` entity with new fields: `name`, `description`, `startDate`, `endDate`, `forAccountType`, `createdBy`
- [x] T003 Implement `UserVoucher` JPA entity with all fields and relationships
- [x] T004 Implement `UserVoucherRepository` with query methods: `findByUserId()`, `findByUserIdAndVoucherId()`, `findByUserIdAndIsUsedFalse()`
- [x] T005 Update `VoucherRepository` with query for available vouchers (filtered by account type, date range, usage)

## Phase 2: Voucher Store Service (Customer)
- [x] T006 Implement `VoucherStoreService.getAvailableVouchers()` — query vouchers by: `isActive=true`, date range, `currentUsage < maxUsage`, account type match; return paginated `VoucherStoreResponse`
- [x] T007 Implement `VoucherStoreService.claimVoucher()` — validate voucher exists and is available, check account type, check not already claimed, check usage remaining; create `UserVoucher`; return `ClaimVoucherResponse`
- [x] T008 Implement `VoucherStoreService.getMyVouchers()` — list user's claimed vouchers from `user_vouchers` with voucher details
- [x] T009 Implement idempotency: catch unique constraint violation on claim and return `VOUCHER_ALREADY_CLAIMED`

## Phase 3: Voucher Admin Service
- [x] T010 Implement `VoucherAdminService.createVoucher()` — validate fields, check code uniqueness, set defaults (currentUsage=0), save
- [x] T011 Implement `VoucherAdminService.updateVoucher()` — partial update, cannot decrease `currentUsage`
- [x] T012 Implement `VoucherAdminService.deactivateVoucher()` — set `isActive=false`, does NOT affect existing `UserVoucher` records
- [x] T013 Implement `VoucherAdminService.listVouchers()` — admin list with filters (status, accountType), pagination
- [x] T014 Implement `VoucherAdminService.getVoucherStats()` — count claims, count redemptions, sum discount given (requires joining user_vouchers + bookings)

## Phase 4: Controllers
- [x] T015 Create `VoucherStoreController` — `GET /api/v1/vouchers/available`, `POST /api/v1/users/me/vouchers/claim/{code}`, `GET /api/v1/users/me/vouchers`
- [x] T016 Create `AdminVoucherController` — `POST /api/v1/admin/vouchers`, `PUT /api/v1/admin/vouchers/{id}`, `DELETE /api/v1/admin/vouchers/{id}`, `GET /api/v1/admin/vouchers`, `GET /api/v1/admin/vouchers/{id}/stats`
- [x] T017 Secure: store/claim/wallet → authenticated; admin → ADMIN role

## Phase 5: Payment Integration
- [x] T018 Update `PaymentServiceImpl` voucher validation — when applying voucher, verify `UserVoucher` exists for this user + voucher (add `UserVoucherRepository` dependency)
- [x] T019 Update voucher redemption on payment success — increment `currentUsage`, set `UserVoucher.isUsed=true`, record `bookingId` and `usedAt`
- [x] T020 Handle `VOUCHER_NOT_CLAIMED` error when voucher applied but not in user's wallet

## Phase 6: Error Handling
- [x] T021 Add voucher-specific exceptions to `GlobalExceptionHandler`: `VoucherNotFoundException`, `VoucherNotAvailableException`, `VoucherAlreadyClaimedException`, `VoucherNotForAccountTypeException`, `VoucherExhaustedException`
- [x] T022 Handle `MINIMUM_BOOKING_VALUE_NOT_MET` for vouchers with `minBookingValue` during checkout

## Phase 7: Testing
- [x] T023 Write `VoucherStoreServiceTest.java` — unit tests: browse filters (account type, date, usage), claim success, claim twice → exception, claim wrong account type → exception, claim exhausted voucher → exception
- [x] T024 Write `VoucherAdminServiceTest.java` — admin CRUD, deactivate, stats calculation
- [x] T025 Write `VoucherStoreControllerTest.java` — controller tests: unauthorized → 401, non-admin → 403 on admin endpoints, claim → 200
- [x] T026 Verify all new acceptance criteria (AC-029 to AC-036) are covered by tests
