-- V11__Reporting_indexes.sql
-- Optimizations for Booking Statistics (STT 24) and Revenue Reports (STT 25)

-- Index for searching and grouping bookings by creation date and status
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('bookings') AND name = 'idx_bookings_created_at_status')
BEGIN
    CREATE INDEX idx_bookings_created_at_status 
    ON bookings (created_at, status);
END;

-- Index for payment checks, filtering on status and creation date, covering amount and booking_id
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('payments') AND name = 'idx_payments_status_created_at')
BEGIN
    CREATE INDEX idx_payments_status_created_at 
    ON payments (status, created_at) 
    INCLUDE (amount, booking_id);
END;
