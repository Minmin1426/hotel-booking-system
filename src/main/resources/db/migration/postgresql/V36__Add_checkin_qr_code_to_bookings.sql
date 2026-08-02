-- V36__Add_checkin_qr_code_to_bookings.sql (PostgreSQL)
-- Module: booking

ALTER TABLE bookings ADD COLUMN IF NOT EXISTS checkin_qr_code VARCHAR(255);
ALTER TABLE bookings ADD COLUMN IF NOT EXISTS checkin_qr_signature VARCHAR(64);

-- Populate checkin_qr_code for existing confirmed/paid bookings if null
UPDATE bookings 
SET checkin_qr_code = 'CHK-' || booking_code,
    checkin_qr_signature = MD5('CHK-' || booking_code || COALESCE(created_at::text, ''))
WHERE checkin_qr_code IS NULL AND status IN ('CONFIRMED', 'COMPLETED');
