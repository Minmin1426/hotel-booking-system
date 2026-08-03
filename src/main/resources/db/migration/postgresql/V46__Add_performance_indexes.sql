-- V46__Add_performance_indexes.sql (PostgreSQL)
-- Add targeted index coverage to drastically accelerate database queries

CREATE INDEX IF NOT EXISTS idx_bookings_user_status ON bookings (user_id, status);
CREATE INDEX IF NOT EXISTS idx_bookings_hotel_dates ON bookings (hotel_id, check_in_date, check_out_date);
CREATE INDEX IF NOT EXISTS idx_rooms_hotel_status ON rooms (hotel_id, status);
CREATE INDEX IF NOT EXISTS idx_payments_booking_status ON payments (booking_id, status);
CREATE INDEX IF NOT EXISTS idx_vouchers_active_dates ON vouchers (is_active, start_date, end_date);
