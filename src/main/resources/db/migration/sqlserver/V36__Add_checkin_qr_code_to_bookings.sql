-- V36__Add_checkin_qr_code_to_bookings.sql (SQL Server)
-- Module: booking

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('bookings') AND name = 'checkin_qr_code')
BEGIN
    ALTER TABLE bookings ADD checkin_qr_code NVARCHAR(255) NULL;
    ALTER TABLE bookings ADD checkin_qr_signature NVARCHAR(64) NULL;
END;
