-- V50__Drop_ticket_type_column_from_meal_tickets.sql
-- Drop the redundant ticket_type column from meal_tickets table

ALTER TABLE meal_tickets DROP COLUMN IF EXISTS ticket_type CASCADE;
