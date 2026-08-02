-- V33__Align_meal_ticket_tables_postgresql.sql
-- Description: Align meal ticket tables in PostgreSQL with SQL Server version and Java Entities

-- 1. Drop existing meal_tickets table if it exists (since it has incorrect schema)
DROP TABLE IF EXISTS meal_ticket_audit_log;
DROP TABLE IF EXISTS meal_tickets;
DROP TABLE IF EXISTS meal_ticket_types;

-- 2. Create meal_ticket_types table
CREATE TABLE meal_ticket_types (
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

-- 3. Seed default meal ticket types
INSERT INTO meal_ticket_types (code, name, description, default_valid_days, default_price) VALUES
('BREAKFAST_BUFFET', 'Breakfast Buffet', 'Complimentary breakfast buffet at the main restaurant', 1, 350000),
('LUNCH_BUFFET', 'Lunch Buffet', 'Complimentary lunch buffet', 1, 550000),
('DINNER_BUFFET', 'Dinner Buffet', 'Complimentary dinner buffet', 1, 750000),
('ROOM_SERVICE', 'Room Service', 'Room service meal voucher', 1, 900000),
('MINIBAR_VOUCHER', 'Minibar Voucher', 'Minibar credit voucher', 7, 200000),
('SPA_VOUCHER', 'Spa Voucher', 'Spa treatment voucher', 30, 1500000);

-- 4. Create meal_tickets table
CREATE TABLE meal_tickets (
    ticket_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    booking_id BIGINT REFERENCES bookings(booking_id) ON DELETE SET NULL,
    ticket_type VARCHAR(30) NOT NULL REFERENCES meal_ticket_types(code),
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    qr_signature VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNUSED',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    consumed_by_staff_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    issued_by BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_meal_tickets_user ON meal_tickets(user_id);
CREATE INDEX idx_meal_tickets_status ON meal_tickets(status);
CREATE INDEX idx_meal_tickets_expires ON meal_tickets(expires_at);
CREATE INDEX idx_meal_tickets_user_status ON meal_tickets(user_id, status);
CREATE INDEX idx_meal_tickets_booking ON meal_tickets(booking_id);

-- 5. Create meal_ticket_audit_log table
CREATE TABLE meal_ticket_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES meal_tickets(ticket_id) ON DELETE CASCADE,
    action VARCHAR(20) NOT NULL,
    actor_user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    metadata TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_ticket ON meal_ticket_audit_log(ticket_id);
CREATE INDEX idx_audit_timestamp ON meal_ticket_audit_log(timestamp DESC);

-- 6. Create ops_meal_tickets table for restaurant operations
CREATE TABLE IF NOT EXISTS ops_meal_tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_code VARCHAR(100) NOT NULL UNIQUE,
    booking_id BIGINT,
    guest_name VARCHAR(255) NOT NULL,
    room_number VARCHAR(50),
    meal_package_id BIGINT NOT NULL,
    package_name VARCHAR(255) NOT NULL,
    total_meals INT NOT NULL,
    remaining_meals INT NOT NULL,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
