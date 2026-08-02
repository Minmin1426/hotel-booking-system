-- V20__Extend_operations_and_group_allocation.sql
-- Migration for Member 2: Hotel & Restaurant Operations, Group Allocation (SQL Server)

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_restaurant_areas')
BEGIN
    CREATE TABLE ops_restaurant_areas (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        hotel_id BIGINT NOT NULL,
        area_name NVARCHAR(255) NOT NULL,
        seating_capacity INT NOT NULL,
        table_count INT NOT NULL,
        kitchen_capacity INT,
        food_safety_cert_url NVARCHAR(MAX),
        status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_room_group_allotments')
BEGIN
    CREATE TABLE ops_room_group_allotments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        hotel_id BIGINT NOT NULL,
        room_type NVARCHAR(100) NOT NULL,
        total_rooms_available INT NOT NULL,
        max_group_quota INT NOT NULL,
        current_allocated_count INT NOT NULL DEFAULT 0,
        group_base_price DECIMAL(18,2),
        notes NVARCHAR(MAX),
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_room_matrix_states')
BEGIN
    CREATE TABLE ops_room_matrix_states (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        hotel_id BIGINT NOT NULL,
        room_id BIGINT,
        room_number NVARCHAR(50) NOT NULL,
        floor INT NOT NULL,
        room_type NVARCHAR(100) NOT NULL,
        status NVARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
        current_guest_name NVARCHAR(255),
        group_name NVARCHAR(255),
        assigned_booking_id BIGINT,
        last_housekeeping_at DATETIME2,
        notes NVARCHAR(MAX),
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_meal_packages')
BEGIN
    CREATE TABLE ops_meal_packages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        hotel_id BIGINT NOT NULL,
        package_code NVARCHAR(100) NOT NULL UNIQUE,
        package_name NVARCHAR(255) NOT NULL,
        category NVARCHAR(50) NOT NULL,
        price_per_pax DECIMAL(18,2) NOT NULL,
        dishes_description NVARCHAR(MAX),
        is_active BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_meal_tickets')
BEGIN
    CREATE TABLE ops_meal_tickets (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        ticket_code NVARCHAR(100) NOT NULL UNIQUE,
        booking_id BIGINT,
        guest_name NVARCHAR(255) NOT NULL,
        room_number NVARCHAR(50),
        meal_package_id BIGINT NOT NULL,
        package_name NVARCHAR(255) NOT NULL,
        total_meals INT NOT NULL,
        remaining_meals INT NOT NULL,
        valid_from DATETIME2,
        valid_until DATETIME2,
        status NVARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_group_pricing_rules')
BEGIN
    CREATE TABLE ops_group_pricing_rules (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        hotel_id BIGINT NOT NULL,
        min_rooms INT NOT NULL,
        discount_percent DECIMAL(5,2) NOT NULL,
        weekend_surcharge_percent DECIMAL(5,2),
        peak_season_multiplier DECIMAL(5,2),
        is_active BIT NOT NULL DEFAULT 1,
        description NVARCHAR(MAX),
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_cancellation_requests')
BEGIN
    CREATE TABLE ops_cancellation_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        booking_code NVARCHAR(100) NOT NULL,
        hotel_id BIGINT NOT NULL,
        customer_name NVARCHAR(255) NOT NULL,
        customer_phone NVARCHAR(50),
        reason NVARCHAR(MAX),
        total_booking_amount DECIMAL(18,2) NOT NULL,
        calculated_refund_amount DECIMAL(18,2) NOT NULL,
        refund_percentage INT,
        status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
        partner_note NVARCHAR(MAX),
        processed_at DATETIME2,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ops_hotel_approval_requests')
BEGIN
    CREATE TABLE ops_hotel_approval_requests (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        hotel_name NVARCHAR(255) NOT NULL,
        location NVARCHAR(MAX) NOT NULL,
        contact_email NVARCHAR(255),
        contact_phone NVARCHAR(50),
        food_safety_cert_number NVARCHAR(100),
        cert_expiry_date DATE,
        cert_document_url NVARCHAR(MAX),
        restaurant_seating_capacity INT,
        status NVARCHAR(50) NOT NULL DEFAULT 'PENDING',
        admin_comment NVARCHAR(MAX),
        reviewed_at DATETIME2,
        created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
        updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
    );
END;
