-- V37__Fix_payments_schema_and_add_missing_columns.sql (PostgreSQL)
-- Idempotent additions only. Actual structural fixes (paid_at rename, currency drop,
-- refund_status restore) are consolidated in V38.

-- Add all missing payment columns (idempotent - safe if already added by V35/V36)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS countdown_end_time TIMESTAMP;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS deposit_ratio DECIMAL(5,4) NOT NULL DEFAULT 0;
ALTER TABLE payments ADD COLUMN IF NOT EXISTS meal_refund_amount DECIMAL(18,2);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS invoice_company_name VARCHAR(255);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS invoice_tax_id VARCHAR(50);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS invoice_company_address VARCHAR(500);
ALTER TABLE payments ADD COLUMN IF NOT EXISTS invoice_company_email VARCHAR(255);
