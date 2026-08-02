-- V3__Add_phone_and_identification.sql
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20) NULL;
ALTER TABLE users ADD COLUMN identification_number VARCHAR(50) NULL;
