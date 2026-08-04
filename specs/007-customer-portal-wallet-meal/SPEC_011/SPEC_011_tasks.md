# Tasks: 011-loyalty-membership-tiers

## Phase 1: Database & Entities
- [x] T001 Create Flyway migration `V24__Loyalty_membership_tiers.sql` — add `current_tier`, `tier_evaluated_at` to users; create `tier_definitions`, `loyalty_point_ledger`, `tier_history` tables; seed 8 default tiers
- [x] T002 Extend `User.java` entity with `currentTier`, `tierEvaluatedAt`
- [x] T003 Implement `TierDefinition` entity and repository
- [x] T004 Implement `LoyaltyPointLedger` entity and repository
- [x] T005 Implement `TierHistory` entity and repository

## Phase 2: Loyalty Service
- [x] T006 Implement `LoyaltyService.awardPoints()` — calculate `floor(amount × multiplier)`, append to ledger, update running balance
- [x] T007 Implement `LoyaltyService.evaluateTier()` — compute 12-month rolling spend, find matching tier definition, promote/demote as needed, record history
- [x] T008 Implement `LoyaltyService.getMyTier()` — return current tier, multiplier, lifetime spend, points, next-tier progress
- [x] T009 Implement `LoyaltyService.getMyTierHistory()` — paginated history
- [x] T010 Implement `LoyaltyService.adjustTier()` — admin manual override, record history with reason
- [x] T011 Implement `LoyaltyService.getTierDefinitions()` — admin list all tiers

## Phase 3: Payment Integration Hook
- [x] T012 Update `PaymentServiceImpl.verifyPayment()` — after marking payment SUCCESS, call `LoyaltyService.awardPoints()` then `LoyaltyService.evaluateTier()`
- [x] T013 Ensure transactional consistency: payment update, points award, tier evaluation all in same transaction

## Phase 4: Scheduled Job
- [x] T014 Implement `TierEvaluationScheduler.dailyDemotion()` — runs at 2 AM daily, evaluates all active users for demotion based on rolling 12-month spend
- [x] T015 Annotate with `@Scheduled(cron = "0 0 2 * * *")`

## Phase 5: Controllers
- [x] T016 Create `LoyaltyController` — `GET /api/v1/users/me/tier`, `GET /api/v1/users/me/tier/history`
- [x] T017 Create `AdminLoyaltyController` — `GET /api/v1/admin/tier-definitions`, `PUT /api/v1/admin/tier-definitions/{id}`, `PUT /api/v1/admin/users/{id}/tier`, `GET /api/v1/admin/users/{id}/points-ledger`

## Phase 6: Error Handling
- [x] T018 Handle `INVALID_TIER` (unknown tier name) → 400
- [x] T019 Handle non-admin manual adjust → 403
- [x] T020 Add `LoyaltyException` to GlobalExceptionHandler

## Phase 7: Testing
- [x] T021 Write `LoyaltyServiceTest.java` — points calc, auto-promote at threshold, auto-demote, manual override, history recording
- [x] T022 Write `LoyaltyControllerTest.java` — controller tests for /me/tier, /me/tier/history
- [x] T023 Verify all new acceptance criteria (AC-040 to AC-045) are covered
