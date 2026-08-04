-- V48__Fix_physical_wristbands_columns.sql (PostgreSQL)
ALTER TABLE physical_wristbands DROP COLUMN IF EXISTS guest_name;
ALTER TABLE physical_wristbands ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE physical_wristbands ADD COLUMN IF NOT EXISTS color_code VARCHAR(30) NOT NULL DEFAULT 'BLUE';
ALTER TABLE physical_wristbands ADD COLUMN IF NOT EXISTS package_name VARCHAR(100);
ALTER TABLE physical_wristbands ADD COLUMN IF NOT EXISTS returned_at TIMESTAMP;
ALTER TABLE physical_wristbands ADD COLUMN IF NOT EXISTS issued_by_staff_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE physical_wristbands ADD COLUMN IF NOT EXISTS notes TEXT;
