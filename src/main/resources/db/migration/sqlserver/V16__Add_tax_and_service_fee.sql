-- Add service_fee and taxes columns to bookings table
ALTER TABLE bookings
ADD service_fee DECIMAL(18,2) DEFAULT 0.00,
    taxes DECIMAL(18,2) DEFAULT 0.00;
