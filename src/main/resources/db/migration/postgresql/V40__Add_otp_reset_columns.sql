-- V40__Add_otp_reset_columns.sql
-- Module: 007-customer-portal-profile (auth)

ALTER TABLE users
ADD COLUMN IF NOT EXISTS otp_code VARCHAR(255);
ALTER TABLE users
ADD COLUMN IF NOT EXISTS otp_expiry TIMESTAMP NULL;
