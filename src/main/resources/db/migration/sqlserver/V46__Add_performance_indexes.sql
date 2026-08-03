-- V46__Add_performance_indexes.sql (SQL Server)
-- Add targeted index coverage to drastically accelerate database queries

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_bookings_user_status' AND object_id = OBJECT_ID('bookings'))
    CREATE INDEX idx_bookings_user_status ON bookings (user_id, status);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_bookings_hotel_dates' AND object_id = OBJECT_ID('bookings'))
    CREATE INDEX idx_bookings_hotel_dates ON bookings (hotel_id, check_in_date, check_out_date);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_rooms_hotel_status' AND object_id = OBJECT_ID('rooms'))
    CREATE INDEX idx_rooms_hotel_status ON rooms (hotel_id, status);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_payments_booking_status' AND object_id = OBJECT_ID('payments'))
    CREATE INDEX idx_payments_booking_status ON payments (booking_id, status);

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_vouchers_active_dates' AND object_id = OBJECT_ID('vouchers'))
    CREATE INDEX idx_vouchers_active_dates ON vouchers (is_active, start_date, end_date);
