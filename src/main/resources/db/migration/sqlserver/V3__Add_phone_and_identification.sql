-- V3: Add phone number and identification number columns to users table
ALTER TABLE users ADD phone_number VARCHAR(20) NULL;
ALTER TABLE users ADD identification_number VARCHAR(50) NULL;
