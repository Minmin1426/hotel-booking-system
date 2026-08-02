-- Add service_fee and taxes columns to bookings table
ALTER TABLE bookings
ADD COLUMN service_fee DECIMAL(18,2) DEFAULT 0.00,
ADD COLUMN taxes DECIMAL(18,2) DEFAULT 0.00;
