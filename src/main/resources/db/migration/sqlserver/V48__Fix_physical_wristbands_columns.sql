-- V48__Fix_physical_wristbands_columns.sql (SQL Server)
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'guest_name')
BEGIN
    ALTER TABLE physical_wristbands DROP COLUMN guest_name;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'user_id')
BEGIN
    ALTER TABLE physical_wristbands ADD user_id BIGINT NULL;
    ALTER TABLE physical_wristbands ADD CONSTRAINT fk_physical_wristbands_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'color_code')
BEGIN
    ALTER TABLE physical_wristbands ADD color_code NVARCHAR(30) NOT NULL DEFAULT 'BLUE';
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'package_name')
BEGIN
    ALTER TABLE physical_wristbands ADD package_name NVARCHAR(100) NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'returned_at')
BEGIN
    ALTER TABLE physical_wristbands ADD returned_at DATETIME2 NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'issued_by_staff_id')
BEGIN
    ALTER TABLE physical_wristbands ADD issued_by_staff_id BIGINT NULL;
    ALTER TABLE physical_wristbands ADD CONSTRAINT fk_physical_wristbands_staff FOREIGN KEY (issued_by_staff_id) REFERENCES users(user_id) ON DELETE SET NULL;
END;

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('physical_wristbands') AND name = 'notes')
BEGIN
    ALTER TABLE physical_wristbands ADD notes NVARCHAR(MAX) NULL;
END;
