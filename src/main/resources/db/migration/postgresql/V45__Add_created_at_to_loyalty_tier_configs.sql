-- V45__Add_created_at_to_loyalty_tier_configs.sql (PostgreSQL)
-- Ensure 'created_at' column exists in loyalty_tier_configs table

ALTER TABLE loyalty_tier_configs ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
