-- V34__Add_loyalty_columns_to_users.sql (PostgreSQL)
-- Fix: User JPA entity expects loyalty columns directly on users table,
-- but V24 only created loyalty_profiles table. Add missing columns here.

ALTER TABLE users ADD COLUMN IF NOT EXISTS current_tier VARCHAR(20) DEFAULT 'BRONZE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS tier_evaluated_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_vip BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS vip_marked_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS vip_marked_by BIGINT;

CREATE INDEX IF NOT EXISTS idx_users_current_tier ON users(current_tier);
CREATE INDEX IF NOT EXISTS idx_users_is_vip ON users(is_vip);
