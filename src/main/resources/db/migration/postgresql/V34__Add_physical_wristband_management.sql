-- V34__Add_physical_wristband_management.sql
-- Description: Create table for Physical Wristband Management for Hotel Meal & Service Control

CREATE TABLE IF NOT EXISTS physical_wristbands (
    wristband_id BIGSERIAL PRIMARY KEY,
    wristband_code VARCHAR(50) NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    color_code VARCHAR(30) NOT NULL DEFAULT 'BLUE',
    package_name VARCHAR(100) DEFAULT 'Breakfast Buffet',
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    returned_at TIMESTAMP,
    issued_by_staff_id BIGINT REFERENCES users(user_id) ON DELETE SET NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wristband_code ON physical_wristbands(wristband_code);
CREATE INDEX IF NOT EXISTS idx_wristband_booking_id ON physical_wristbands(booking_id);
