-- V24__Loyalty_membership_tiers.sql (SQL Server variant)
-- Description: Tiered loyalty membership — tier_definitions, loyalty_point_ledger, tier_history
-- Module: 011-loyalty-membership-tiers

-- 1. Add tier fields to users
ALTER TABLE users ADD current_tier NVARCHAR(50) NOT NULL DEFAULT 'BRONZE';
ALTER TABLE users ADD tier_evaluated_at DATETIME2 NULL;

-- 2. Tier definitions
CREATE TABLE tier_definitions (
    tier_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE,
    account_type NVARCHAR(30) NOT NULL, -- 'CUSTOMER' | 'CORPORATE_MEMBER'
    min_annual_spend DECIMAL(18,2) NOT NULL DEFAULT 0,
    point_multiplier DECIMAL(3,2) NOT NULL DEFAULT 1.00,
    max_spending_limit DECIMAL(18,2) DEFAULT NULL,
    priority_support BIT NOT NULL DEFAULT 0,
    exclusive_voucher_access BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Loyalty point ledger
CREATE TABLE loyalty_point_ledger (
    ledger_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    booking_id BIGINT,
    points_earned INT NOT NULL,
    multiplier_used DECIMAL(3,2) NOT NULL,
    running_balance BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_loyalty_ledger_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_loyalty_ledger_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE NO ACTION
);

-- 4. Tier history
CREATE TABLE tier_history (
    history_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    previous_tier NVARCHAR(50),
    new_tier NVARCHAR(50) NOT NULL,
    reason NVARCHAR(50) NOT NULL, -- 'AUTO_PROMOTION' | 'AUTO_DEMOTION' | 'ADMIN_ADJUSTMENT'
    changed_by BIGINT NULL,
    created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tier_history_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_tier_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(user_id) ON DELETE NO ACTION
);

-- 5. Seed tier definitions
INSERT INTO tier_definitions (name, account_type, min_annual_spend, point_multiplier, priority_support, exclusive_voucher_access)
VALUES
    ('BRONZE',     'CUSTOMER', 0,           1.00, 0, 0),
    ('SILVER',     'CUSTOMER', 5000000,    1.25, 0, 0),
    ('GOLD',       'CUSTOMER', 20000000,   1.50, 1, 0),
    ('PLATINUM',   'CUSTOMER', 50000000,   2.00, 1, 1),
    ('BRONZE_BUSINESS',     'CORPORATE_MEMBER', 0,           1.00, 0, 0),
    ('SILVER_BUSINESS',      'CORPORATE_MEMBER', 30000000,   1.50, 0, 0),
    ('GOLD_BUSINESS',        'CORPORATE_MEMBER', 100000000,  2.00, 1, 0),
    ('PLATINUM_BUSINESS',    'CORPORATE_MEMBER', 500000000,  3.00, 1, 1);

-- 6. Indexes
CREATE INDEX idx_users_tier ON users(current_tier);
CREATE INDEX idx_loyalty_ledger_user ON loyalty_point_ledger(user_id);
CREATE INDEX idx_loyalty_ledger_booking ON loyalty_point_ledger(booking_id);
CREATE INDEX idx_loyalty_ledger_created ON loyalty_point_ledger(created_at DESC);
CREATE INDEX idx_tier_history_user ON tier_history(user_id);
CREATE INDEX idx_tier_history_created ON tier_history(created_at DESC);
