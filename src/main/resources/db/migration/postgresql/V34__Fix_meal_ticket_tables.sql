-- V34__Fix_meal_ticket_tables.sql (PostgreSQL)
-- Module: mealticket

-- 1. Create meal_ticket_types table if not exists
CREATE TABLE IF NOT EXISTS meal_ticket_types (
    type_id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    default_valid_days INT NOT NULL DEFAULT 30,
    default_price DECIMAL(18, 2) NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed default meal ticket types
INSERT INTO meal_ticket_types (code, name, description, default_valid_days, default_price) VALUES
('BREAKFAST_BUFFET', 'Breakfast Buffet', 'Complimentary breakfast buffet at the main restaurant', 1, 350000),
('LUNCH_BUFFET', 'Lunch Buffet', 'Complimentary lunch buffet', 1, 550000),
('DINNER_BUFFET', 'Dinner Buffet', 'Complimentary dinner buffet', 1, 750000),
('ROOM_SERVICE', 'Room Service', 'Room service meal voucher', 1, 900000),
('MINIBAR_VOUCHER', 'Minibar Voucher', 'Minibar credit voucher', 7, 200000),
('SPA_VOUCHER', 'Spa Voucher', 'Spa treatment voucher', 30, 1500000)
ON CONFLICT (code) DO NOTHING;

-- 2. Ensure meal_tickets table has all necessary columns
CREATE TABLE IF NOT EXISTS meal_tickets (
    ticket_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    qr_signature VARCHAR(64) DEFAULT '',
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add columns if missing
ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS booking_id BIGINT REFERENCES bookings(booking_id) ON DELETE SET NULL;
ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS ticket_type_id BIGINT REFERENCES meal_ticket_types(type_id);
ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS qr_signature VARCHAR(64) DEFAULT '';
ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS consumed_by_staff_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS issued_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL;
ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS notes TEXT;

-- Populate ticket_type_id for existing rows if null
UPDATE meal_tickets 
SET ticket_type_id = (SELECT type_id FROM meal_ticket_types WHERE code = 'BREAKFAST_BUFFET' LIMIT 1)
WHERE ticket_type_id IS NULL;

-- 3. Create meal_ticket_audit_log table if not exists
CREATE TABLE IF NOT EXISTS meal_ticket_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES meal_tickets(ticket_id) ON DELETE CASCADE,
    action VARCHAR(20) NOT NULL,
    actor_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    metadata TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_ticket ON meal_ticket_audit_log(ticket_id);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON meal_ticket_audit_log(timestamp DESC);
