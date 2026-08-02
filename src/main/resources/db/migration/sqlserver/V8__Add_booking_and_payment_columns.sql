-- V8__Add_booking_and_payment_columns.sql
-- Description: Add support columns and create room_locks table for booking flow (UC-12, UC-14, UC-33)

-- Add confirmed_at and notes to bookings table
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'confirmed_at')
BEGIN
    ALTER TABLE bookings ADD confirmed_at DATETIME2 NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'notes')
BEGIN
    ALTER TABLE bookings ADD notes NVARCHAR(MAX) NULL;
END;

-- Add transaction_id to payments table
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('payments') AND name = 'transaction_id')
BEGIN
    ALTER TABLE payments ADD transaction_id VARCHAR(255) NULL;
END;

-- Create room_locks table
IF OBJECT_ID('room_locks', 'U') IS NULL
BEGIN
    CREATE TABLE room_locks (
        lock_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        room_id BIGINT NOT NULL,
        booking_id BIGINT NOT NULL,
        locked_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        expires_at DATETIME2 NOT NULL,
        CONSTRAINT fk_room_locks_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
        CONSTRAINT fk_room_locks_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
    );
    CREATE INDEX idx_room_locks_room_id ON room_locks(room_id);
    CREATE INDEX idx_room_locks_booking_id ON room_locks(booking_id);
END;
