-- V43__Add_physical_wristband_management.sql (PostgreSQL)

CREATE TABLE IF NOT EXISTS physical_wristbands (
    wristband_id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(booking_id) ON DELETE CASCADE,
    wristband_code VARCHAR(100) NOT NULL UNIQUE,
    guest_name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wristband_booking ON physical_wristbands(booking_id);
CREATE INDEX IF NOT EXISTS idx_wristband_code ON physical_wristbands(wristband_code);
