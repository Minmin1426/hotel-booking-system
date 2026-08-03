-- V50__Drop_ticket_type_column_from_meal_tickets.sql (SQL Server)
-- Drop the redundant ticket_type column from meal_tickets table

IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('meal_tickets') AND name = 'ticket_type')
BEGIN
    DECLARE @ConstraintName NVARCHAR(200);
    SELECT @ConstraintName = f.name
    FROM sys.foreign_keys f
    JOIN sys.foreign_key_columns fc ON f.object_id = fc.constraint_object_id
    JOIN sys.columns c ON fc.parent_column_id = c.column_id AND fc.parent_object_id = c.object_id
    WHERE fc.parent_object_id = OBJECT_ID('meal_tickets') AND c.name = 'ticket_type';

    IF @ConstraintName IS NOT NULL
    BEGIN
        DECLARE @DropSql NVARCHAR(MAX) = 'ALTER TABLE meal_tickets DROP CONSTRAINT ' + QUOTENAME(@ConstraintName);
        EXEC sp_executesql @DropSql;
    END

    ALTER TABLE meal_tickets DROP COLUMN ticket_type;
END
