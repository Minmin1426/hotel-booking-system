-- V39__Add_reason_to_loyalty_point_ledger.sql
-- Module: 015-admin-customer-management (loyalty)

ALTER TABLE loyalty_point_ledger
ADD COLUMN IF NOT EXISTS reason VARCHAR(255);
