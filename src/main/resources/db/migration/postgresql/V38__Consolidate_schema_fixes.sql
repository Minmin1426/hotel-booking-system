-- V38__Consolidate_schema_fixes.sql (PostgreSQL)
-- Consolidates all PostgreSQL schema fixes from V34-V37 and resolves structural gaps.
-- Sections 0-27 covering: wallets, audit logs, vouchers, payments, user_vouchers,
-- wallet_transactions, loyalty_tier_configs, missing tables, topup_configs, etc.
-- ============================
-- 0. WALLET table rename
-- V25 created 'customer_wallets' but Wallet entity maps to 'wallets'.
-- Uses DROP+CREATE (not RENAME) to safely handle FK constraints.
-- NOTE: Dev-only approach — existing wallet data is lost.
-- ============================
DROP TABLE IF EXISTS customer_wallets CASCADE;
CREATE TABLE IF NOT EXISTS wallets (
    wallet_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_wallet_user ON wallets(user_id);
-- ============================
-- 1. PAYMENT AUDIT LOG fixes
-- V16 dropped action and response_payload; restore them.
-- ============================
ALTER TABLE payment_audit_logs ADD COLUMN IF NOT EXISTS action VARCHAR(100);
ALTER TABLE payment_audit_logs ADD COLUMN IF NOT EXISTS response_payload TEXT;

-- ============================
-- 2. VOUCHERS column fixes
-- V9 never added voucher_type, for_account_type, combo_meal_benefit columns.
-- ============================
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS voucher_type VARCHAR(50) DEFAULT 'ROOM';
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS for_account_type VARCHAR(30) DEFAULT 'ALL';
ALTER TABLE vouchers ADD COLUMN IF NOT EXISTS combo_meal_benefit VARCHAR(255);

-- ============================
-- 3. PAYMENTS column fixes
-- ============================
ALTER TABLE payments ADD COLUMN IF NOT EXISTS is_deposit BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS gateway_ref VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS refund_status VARCHAR(50);

-- ============================
-- 4. USER_VOUCHERS column fix
-- V23 created 'status' but entity uses 'is_used'.
-- ============================
ALTER TABLE user_vouchers ADD COLUMN IF NOT EXISTS is_used BOOLEAN NOT NULL DEFAULT FALSE;

-- ============================
-- 5. WALLET_TRANSACTIONS column fixes
-- V25 created table without balance_before and related_booking_id.
-- ============================
ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS balance_before DECIMAL(18,2) NOT NULL DEFAULT 0;
ALTER TABLE wallet_transactions ADD COLUMN IF NOT EXISTS related_booking_id BIGINT;

-- ============================
-- 6. LOYALTY_TIER_CONFIGS schema fix (PostgreSQL)
-- V24 created loyalty_tier_configs with columns: tier_name (PK), min_points, min_spent,
-- discount_percentage, perk_description. Entity TierDefinition expects additional columns.
-- Add missing entity columns to existing table (idempotent).
-- ============================
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS tier_id BIGSERIAL;
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS account_type VARCHAR(30) NOT NULL DEFAULT 'CUSTOMER';
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS min_annual_spend DECIMAL(18,2) NOT NULL DEFAULT 0;
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS point_multiplier DECIMAL(3,2) NOT NULL DEFAULT 1.00;
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS max_spending_limit DECIMAL(18,2);
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS priority_support BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS exclusive_voucher_access BOOLEAN NOT NULL DEFAULT FALSE;

-- ============================
-- 7. TIER_HISTORY table
-- Never created; referenced by loyalty/TierHistory entity.
-- ============================
CREATE TABLE IF NOT EXISTS tier_history (
    history_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    previous_tier VARCHAR(50),
    new_tier VARCHAR(50) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    changed_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tier_history_user ON tier_history(user_id);
CREATE INDEX IF NOT EXISTS idx_tier_history_created ON tier_history(created_at DESC);

-- ============================
-- 8. LOYALTY_POINT_LEDGER table
-- Never created; V24 created loyalty_history with incompatible columns.
-- Entity expects: ledger_id, user_id, booking_id, points_earned,
-- multiplier_used, running_balance, created_at.
-- ============================
CREATE TABLE IF NOT EXISTS loyalty_point_ledger (
    ledger_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    booking_id BIGINT REFERENCES bookings(booking_id) ON DELETE SET NULL,
    points_earned INT NOT NULL,
    multiplier_used DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    running_balance BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ledger_user ON loyalty_point_ledger(user_id);
CREATE INDEX IF NOT EXISTS idx_ledger_booking ON loyalty_point_ledger(booking_id);
CREATE INDEX IF NOT EXISTS idx_ledger_created ON loyalty_point_ledger(created_at DESC);

-- ============================
-- 9. GROUPS table
-- Never created; referenced by wallet/Group entity.
-- ============================
CREATE TABLE IF NOT EXISTS "groups" (
    group_id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(255) NOT NULL,
    owner_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    tax_code VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_groups_owner ON "groups"(owner_user_id);

-- ============================
-- 10. GROUP_MEMBERSHIPS table
-- Never created; referenced by wallet/GroupMembership entity.
-- ============================
CREATE TABLE IF NOT EXISTS group_memberships (
    membership_id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES "groups"(group_id) ON DELETE CASCADE,
    member_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    spending_limit DECIMAL(18,2) DEFAULT 0,
    joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gm_group ON group_memberships(group_id);
CREATE INDEX IF NOT EXISTS idx_gm_member ON group_memberships(member_user_id);

-- ============================
-- 11. SPENDING_LIMITS table
-- Never created; referenced by wallet/topup/SpendingLimit entity.
-- ============================
CREATE TABLE IF NOT EXISTS spending_limits (
    limit_id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES "groups"(group_id) ON DELETE CASCADE,
    member_user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    per_transaction_limit DECIMAL(18,2),
    daily_limit DECIMAL(18,2),
    monthly_limit DECIMAL(18,2),
    effective_from DATE NOT NULL DEFAULT CURRENT_DATE,
    effective_until DATE,
    created_by BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_sl_group ON spending_limits(group_id);
CREATE INDEX IF NOT EXISTS idx_sl_member ON spending_limits(member_user_id);

-- ============================
-- 12. SPENDING_LIMIT_HISTORY table
-- Never created; referenced by wallet/topup/SpendingLimitHistory entity.
-- ============================
CREATE TABLE IF NOT EXISTS spending_limit_history (
    history_id BIGSERIAL PRIMARY KEY,
    limit_id BIGINT NOT NULL REFERENCES spending_limits(limit_id) ON DELETE CASCADE,
    previous_limit DECIMAL(18,2),
    new_limit DECIMAL(18,2),
    changed_by BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_slh_limit ON spending_limit_history(limit_id);

-- ============================
-- 13. SPENDING_TRACKING table
-- Never created; referenced by wallet/topup/SpendingTracking entity.
-- ============================
CREATE TABLE IF NOT EXISTS spending_tracking (
    tracking_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES "groups"(group_id) ON DELETE CASCADE,
    period_type VARCHAR(10) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_spent DECIMAL(18,2) NOT NULL DEFAULT 0,
    UNIQUE (user_id, group_id, period_type, period_start)
);

CREATE INDEX IF NOT EXISTS idx_st_user_group ON spending_tracking(user_id, group_id);

-- ============================
-- 14. PAYOUTS column additions
-- V30 created payouts with only user_id; entity expects hotel_id,
-- period_start, period_end, total_revenue, commission_rate, payout_amount.
-- ============================
ALTER TABLE payouts ADD COLUMN IF NOT EXISTS hotel_id BIGINT;
ALTER TABLE payouts ADD COLUMN IF NOT EXISTS period_start TIMESTAMP;
ALTER TABLE payouts ADD COLUMN IF NOT EXISTS period_end TIMESTAMP;
ALTER TABLE payouts ADD COLUMN IF NOT EXISTS total_revenue DECIMAL(18,2);
ALTER TABLE payouts ADD COLUMN IF NOT EXISTS commission_rate DECIMAL(5,2);
ALTER TABLE payouts ADD COLUMN IF NOT EXISTS payout_amount DECIMAL(18,2);

-- ============================
-- 15. CUSTOMER_ACTIVITY_EVENTS table fix
-- V28 created 'customer_activity_logs' with wrong name and wrong columns.
-- Entity expects 'customer_activity_events' with: event_id, user_id (FK),
-- event_type, event_summary, event_metadata, actor_user_id, created_at.
-- ============================
DROP TABLE IF EXISTS customer_activity_logs;
CREATE TABLE customer_activity_events (
    event_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    event_type VARCHAR(50) NOT NULL,
    event_summary TEXT NOT NULL,
    event_metadata TEXT,
    actor_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cae_user ON customer_activity_events(user_id);
CREATE INDEX IF NOT EXISTS idx_cae_created ON customer_activity_events(created_at DESC);

-- ============================
-- 16. CUSTOMER_NOTES table
-- Never created; referenced by admin/CustomerNote entity.
-- ============================
CREATE TABLE IF NOT EXISTS customer_notes (
    note_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_cn_user ON customer_notes(user_id);
CREATE INDEX IF NOT EXISTS idx_cn_author ON customer_notes(author_id);

-- ============================
-- 17. TOPUP_CONFIGS table (was referenced by TopUpConfig entity, never created in PostgreSQL)
-- V26 only created wallet_topup_methods, not this table.
-- ============================
CREATE TABLE IF NOT EXISTS topup_configs (
    config_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    wallet_id BIGINT NOT NULL UNIQUE REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    threshold_amount DECIMAL(18,2) NOT NULL,
    topup_amount DECIMAL(18,2) NOT NULL,
    payment_method_id VARCHAR(255),
    max_daily_auto_topup INT NOT NULL DEFAULT 5,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tc_user ON topup_configs(user_id);
CREATE INDEX IF NOT EXISTS idx_tc_wallet ON topup_configs(wallet_id);

-- ============================
-- 18. TOPUP_HISTORY table
-- Never created; referenced by wallet/topup/TopUpHistory entity.
-- ============================
CREATE TABLE IF NOT EXISTS topup_history (
    history_id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES wallets(wallet_id) ON DELETE CASCADE,
    amount DECIMAL(18,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'STRIPE',
    stripe_session_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_auto_topup BOOLEAN NOT NULL DEFAULT FALSE,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_tuh_wallet ON topup_history(wallet_id);
CREATE INDEX IF NOT EXISTS idx_tuh_created ON topup_history(created_at DESC);

-- ============================
-- 19. OPS_MEAL_TICKETS column fix
-- V33 created the table but missing valid_from, valid_until, updated_at.
-- ============================
ALTER TABLE ops_meal_tickets ADD COLUMN IF NOT EXISTS valid_from TIMESTAMP;
ALTER TABLE ops_meal_tickets ADD COLUMN IF NOT EXISTS valid_until TIMESTAMP;

-- ============================
-- 20. OPS_CANCELLATION_REQUESTS table
-- Never created; referenced by operations/CancellationRequest entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_cancellation_requests (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    booking_code VARCHAR(100) NOT NULL,
    hotel_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    customer_phone VARCHAR(50),
    reason TEXT,
    total_booking_amount DECIMAL(18,2) NOT NULL,
    calculated_refund_amount DECIMAL(18,2) NOT NULL,
    refund_percentage INT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    partner_note VARCHAR(500),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ocr_status ON ops_cancellation_requests(status);
CREATE INDEX IF NOT EXISTS idx_ocr_hotel ON ops_cancellation_requests(hotel_id);

-- ============================
-- 21. OPS_GROUP_PRICING_RULES table
-- Never created; referenced by operations/GroupPricingRule entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_group_pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    min_rooms INT NOT NULL,
    discount_percent DECIMAL(5,2) NOT NULL,
    weekend_surcharge_percent DECIMAL(5,2),
    peak_season_multiplier DECIMAL(5,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_gpr_hotel ON ops_group_pricing_rules(hotel_id);

-- ============================
-- 22. OPS_HOTEL_APPROVAL_REQUESTS table
-- Never created; referenced by operations/HotelApprovalRequest entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_hotel_approval_requests (
    id BIGSERIAL PRIMARY KEY,
    hotel_name VARCHAR(255) NOT NULL,
    location VARCHAR(500) NOT NULL,
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    food_safety_cert_number VARCHAR(100),
    cert_expiry_date DATE,
    cert_document_url VARCHAR(500),
    restaurant_seating_capacity INT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    admin_comment VARCHAR(500),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_har_status ON ops_hotel_approval_requests(status);

-- ============================
-- 23. OPS_MEAL_PACKAGES table
-- Never created; referenced by operations/MealPackage entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_meal_packages (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    package_code VARCHAR(100) NOT NULL UNIQUE,
    package_name VARCHAR(255) NOT NULL,
    category VARCHAR(30) NOT NULL,
    price_per_pax DECIMAL(18,2) NOT NULL,
    dishes_description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_omp_hotel ON ops_meal_packages(hotel_id);

-- ============================
-- 24. OPS_RESTAURANT_AREAS table
-- Never created; referenced by operations/RestaurantArea entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_restaurant_areas (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    area_name VARCHAR(255) NOT NULL,
    seating_capacity INT NOT NULL,
    table_count INT NOT NULL,
    kitchen_capacity INT,
    food_safety_cert_url VARCHAR(500),
    status VARCHAR(30) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ora_hotel ON ops_restaurant_areas(hotel_id);

-- ============================
-- 25. OPS_ROOM_GROUP_ALLOTMENTS table
-- Never created; referenced by operations/RoomGroupAllotment entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_room_group_allotments (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    total_rooms_available INT NOT NULL,
    max_group_quota INT NOT NULL,
    current_allocated_count INT NOT NULL DEFAULT 0,
    group_base_price DECIMAL(18,2),
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rga_hotel ON ops_room_group_allotments(hotel_id);

-- ============================
-- 26. OPS_ROOM_MATRIX_STATES table
-- Never created; referenced by operations/RoomMatrixState entity.
-- ============================
CREATE TABLE IF NOT EXISTS ops_room_matrix_states (
    id BIGSERIAL PRIMARY KEY,
    hotel_id BIGINT NOT NULL,
    room_id BIGINT,
    room_number VARCHAR(50) NOT NULL,
    floor INT NOT NULL,
    room_type VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    current_guest_name VARCHAR(255),
    group_name VARCHAR(255),
    assigned_booking_id BIGINT,
    last_housekeeping_at TIMESTAMP,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rms_hotel ON ops_room_matrix_states(hotel_id);
CREATE INDEX IF NOT EXISTS idx_rms_room ON ops_room_matrix_states(room_id);

-- ============================
-- 27. BULK_ACTION_LOGS table
-- Never created; referenced by admin/BulkActionLog entity.
-- ============================
CREATE TABLE IF NOT EXISTS bulk_action_logs (
    bulk_action_id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    action_type VARCHAR(50) NOT NULL,
    target_user_ids TEXT NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_bal_admin ON bulk_action_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_bal_created ON bulk_action_logs(created_at DESC);
