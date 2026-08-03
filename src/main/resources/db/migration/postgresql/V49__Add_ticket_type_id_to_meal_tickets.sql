-- V49__Add_ticket_type_id_to_meal_tickets.sql
-- Add missing ticket_type_id column, populate it from ticket_type, then drop the old redundant ticket_type column.

ALTER TABLE meal_tickets ADD COLUMN IF NOT EXISTS ticket_type_id BIGINT REFERENCES meal_ticket_types(type_id);

-- Populate ticket_type_id from ticket_type column if it exists in the database
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='meal_tickets' AND column_name='ticket_type') THEN
        UPDATE meal_tickets mt
        SET ticket_type_id = mtt.type_id
        FROM meal_ticket_types mtt
        WHERE mt.ticket_type = mtt.code
          AND mt.ticket_type_id IS NULL;
    END IF;
END $$;

-- Fallback for any remaining nulls to BREAKFAST_BUFFET type_id
UPDATE meal_tickets
SET ticket_type_id = (SELECT type_id FROM meal_ticket_types WHERE code = 'BREAKFAST_BUFFET' LIMIT 1)
WHERE ticket_type_id IS NULL;

-- Drop the old redundant ticket_type column and its constraints cascade
ALTER TABLE meal_tickets DROP COLUMN IF EXISTS ticket_type CASCADE;
