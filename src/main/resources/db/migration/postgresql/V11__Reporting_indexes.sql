-- V11__Reporting_indexes.sql
-- Optimizations for Booking Statistics (STT 24) and Revenue Reports (STT 25) (PostgreSQL)

CREATE INDEX IF NOT EXISTS idx_bookings_created_at_status ON bookings (created_at, status);

CREATE INDEX IF NOT EXISTS idx_payments_status_created_at ON payments (status, created_at) INCLUDE (amount, booking_id);
