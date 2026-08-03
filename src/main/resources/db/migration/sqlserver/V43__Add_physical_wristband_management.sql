-- V43__Add_physical_wristband_management.sql (SQL Server)

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'physical_wristbands')
BEGIN
    CREATE TABLE physical_wristbands (
        wristband_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        wristband_code NVARCHAR(100) NOT NULL UNIQUE,
        guest_name NVARCHAR(100) NOT NULL,
        status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
        issued_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        created_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME2 NOT NULL DEFAULT CURRENT_TIMESTAMP,
        CONSTRAINT fk_physical_wristbands_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id) ON DELETE CASCADE
    );

    CREATE INDEX idx_wristband_booking ON physical_wristbands(booking_id);
    CREATE INDEX idx_wristband_code ON physical_wristbands(wristband_code);
END;
