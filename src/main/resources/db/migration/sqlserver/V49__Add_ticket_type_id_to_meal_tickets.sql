-- V49__Add_ticket_type_id_to_meal_tickets.sql (SQL Server)
-- Add missing ticket_type_id column and populate it from ticket_type

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('meal_tickets') AND name = 'ticket_type_id')
BEGIN
    ALTER TABLE meal_tickets ADD ticket_type_id BIGINT FOREIGN KEY REFERENCES meal_ticket_types(type_id);
END

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('meal_tickets') AND name = 'ticket_type')
BEGIN
    EXEC sp_executesql N'
        UPDATE mt
        SET mt.ticket_type_id = mtt.type_id
        FROM meal_tickets mt
        JOIN meal_ticket_types mtt ON mt.ticket_type = mtt.code
        WHERE mt.ticket_type_id IS NULL;
    ';
END

UPDATE meal_tickets
SET ticket_type_id = (SELECT TOP 1 type_id FROM meal_ticket_types WHERE code = 'BREAKFAST_BUFFET')
WHERE ticket_type_id IS NULL;
