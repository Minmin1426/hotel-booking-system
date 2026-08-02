-- V35__Fix_meal_ticket_tables.sql (SQL Server)
-- Module: mealticket

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'meal_ticket_types')
BEGIN
    CREATE TABLE meal_ticket_types (
        type_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code NVARCHAR(30) NOT NULL UNIQUE,
        name NVARCHAR(100) NOT NULL,
        description NVARCHAR(MAX),
        default_valid_days INT NOT NULL DEFAULT 30,
        default_price DECIMAL(18, 2) NOT NULL DEFAULT 0,
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        updated_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
    );

    INSERT INTO meal_ticket_types (code, name, description, default_valid_days, default_price) VALUES
    ('BREAKFAST_BUFFET', 'Breakfast Buffet', 'Complimentary breakfast buffet at the main restaurant', 1, 350000),
    ('LUNCH_BUFFET', 'Lunch Buffet', 'Complimentary lunch buffet', 1, 550000),
    ('DINNER_BUFFET', 'Dinner Buffet', 'Complimentary dinner buffet', 1, 750000),
    ('ROOM_SERVICE', 'Room Service', 'Room service meal voucher', 1, 900000),
    ('MINIBAR_VOUCHER', 'Minibar Voucher', 'Minibar credit voucher', 7, 200000),
    ('SPA_VOUCHER', 'Spa Voucher', 'Spa treatment voucher', 30, 1500000);
END;

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('meal_tickets') AND name = 'ticket_type_id')
BEGIN
    ALTER TABLE meal_tickets ADD ticket_type_id BIGINT NULL;
    ALTER TABLE meal_tickets ADD CONSTRAINT FK_meal_tickets_types_id FOREIGN KEY (ticket_type_id) REFERENCES meal_ticket_types(type_id);
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'meal_ticket_audit_log')
BEGIN
    CREATE TABLE meal_ticket_audit_log (
        audit_id BIGINT IDENTITY(1,1) PRIMARY KEY,
        ticket_id BIGINT NOT NULL,
        action NVARCHAR(20) NOT NULL,
        actor_user_id BIGINT,
        metadata NVARCHAR(MAX),
        timestamp DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_audit_tickets_sql FOREIGN KEY (ticket_id) REFERENCES meal_tickets(ticket_id) ON DELETE CASCADE,
        CONSTRAINT FK_audit_users_sql FOREIGN KEY (actor_user_id) REFERENCES users(user_id) ON DELETE SET NULL
    );
END;
