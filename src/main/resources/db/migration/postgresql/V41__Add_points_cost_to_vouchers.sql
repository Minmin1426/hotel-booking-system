-- V41__Add_points_cost_to_vouchers.sql
-- Module: 007-customer-portal-profile

ALTER TABLE vouchers
ADD COLUMN IF NOT EXISTS points_cost INTEGER NULL;
