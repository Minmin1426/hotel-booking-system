-- V6__Seed_hotels_and_rooms.sql
-- Description: Seed sample hotels, hotel images, and rooms for Search & Discovery testing

-- 1. Insert Hotels
INSERT INTO hotels (name, location, description, rating, is_active, created_at, updated_at) VALUES
('InterContinental Landmark72', 'Keangnam Landmark72, Hanoi, Vietnam', 'Spectacular Hanoi city views from the tallest luxury hotel in Vietnam.', 4.9, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sheraton Hanoi Westlake', 'K Nghi Tam, Tay Ho, Hanoi, Vietnam', 'Scenic lakeside luxury resort offering a peaceful escape in Hanoi.', 4.7, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hilton Saigon Riverview', 'Me Linh Square, District 1, Ho Chi Minh City, Vietnam', 'Premium business and leisure hotel overlooking the historic Saigon River.', 4.6, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Pullman Danang Beach Resort', 'Vo Nguyen Giap, Ngu Hanh Son, Da Nang, Vietnam', 'Stunning beachfront resort with premium amenities, infinity pool, and luxury spa.', 4.8, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 2. Insert Hotel Images (using premium royalty-free Unsplash URLs)
-- We need the hotel IDs. Since identity is 1, 2, 3, 4:
INSERT INTO hotel_images (hotel_id, image_url, image_format, created_at, updated_at) VALUES
(1, 'https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80', 'jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80', 'jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80', 'jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80', 'jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'https://images.unsplash.com/photo-1571896349842-33c89424de2d?auto=format&fit=crop&w=800&q=80', 'jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 3. Insert Rooms
-- For Hotel 1: InterContinental Landmark72
INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at) VALUES
(1, 'Standard Single Room', 120.00, '3501', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Deluxe Double Room', 220.00, '3502', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'Presidential Suite', 750.00, '7201', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- For Hotel 2: Sheraton Hanoi Westlake
INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at) VALUES
(2, 'Superior Lakeview Room', 150.00, '101', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Deluxe Gardenview Room', 180.00, '102', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Executive Club Suite', 320.00, '501', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- For Hotel 3: Hilton Saigon Riverview
INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at) VALUES
(3, 'Deluxe Riverview Single', 140.00, '1201', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Executive Double Room', 240.00, '1202', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Grand River Suite', 450.00, '2001', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- For Hotel 4: Pullman Danang Beach Resort
INSERT INTO rooms (hotel_id, room_type, price, room_number, status, created_at, updated_at) VALUES
(4, 'Superior Oceanview Single', 160.00, '201', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Deluxe Poolview Double', 260.00, '202', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Beachfront Villa', 850.00, 'V101', 'AVAILABLE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
