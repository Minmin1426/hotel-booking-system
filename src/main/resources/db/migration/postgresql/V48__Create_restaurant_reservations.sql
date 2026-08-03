CREATE TABLE IF NOT EXISTS restaurant_reservations (
    id BIGSERIAL PRIMARY KEY,
    res_code VARCHAR(20) NOT NULL UNIQUE,
    guest_name VARCHAR(100) NOT NULL,
    guest_phone VARCHAR(20) NOT NULL,
    pkg_title VARCHAR(100) NOT NULL,
    res_date DATE NOT NULL,
    res_time VARCHAR(10) NOT NULL,
    hold_limit VARCHAR(10) NOT NULL,
    guests INT NOT NULL,
    price DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Seed some mock data to begin with
INSERT INTO restaurant_reservations (res_code, guest_name, guest_phone, pkg_title, res_date, res_time, hold_limit, guests, price, status, notes) VALUES
('RES-889012', 'Nguyễn Nhật Minh', '0912345678', 'Suất Buffet Tối Premium ($35)', '2026-08-01', '19:00', '19:15', 2, 70.00, 'HOLDING', 'Bàn gần cửa sổ'),
('RES-772045', 'Trần Văn Nam', '0987654321', 'Set Tiệc Bàn 10 Khách ($180)', '2026-08-01', '18:30', '18:45', 1, 180.00, 'ARRIVED', 'Cần 2 ghế cho bé nhỏ'),
('RES-663011', 'Lê Thị Thu', '0933112233', 'Suất Buffet Sáng Tự Chọn ($15)', '2026-08-01', '08:30', '08:45', 4, 60.00, 'RELEASED', '')
ON CONFLICT (res_code) DO NOTHING;
