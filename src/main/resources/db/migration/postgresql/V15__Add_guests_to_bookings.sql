-- Add adults and children columns to bookings table
ALTER TABLE bookings
ADD COLUMN adults INTEGER NOT NULL DEFAULT 2,
ADD COLUMN children INTEGER NOT NULL DEFAULT 0;
