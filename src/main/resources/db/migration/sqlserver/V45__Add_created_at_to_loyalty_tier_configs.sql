-- V45__Add_created_at_to_loyalty_tier_configs.sql (SQL Server)
-- Ensure 'created_at' column exists in loyalty_tier_configs table

IF NOT EXISTS (
    SELECT 1 FROM sys.columns 
    WHERE object_id = OBJECT_ID('loyalty_tier_configs') AND name = 'created_at'
)
BEGIN
    ALTER TABLE loyalty_tier_configs ADD created_at DATETIME2 NOT NULL DEFAULT GETDATE();
END
