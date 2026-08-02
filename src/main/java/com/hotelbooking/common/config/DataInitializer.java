package com.hotelbooking.common.config;

import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelImage;
import com.hotelbooking.hotel.HotelRepository;
import com.hotelbooking.room.Room;

import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (hotelRepository.count() == 0) {
            log.info("Seeding initial Hotels, Rooms, and Images for H2 database...");
            seedHotelsAndRooms();
        }
        if (userRepository.count() == 0) {
            log.info("Seeding initial Admin & Customer Users...");
            seedUsers();
        }
    }

    private void seedHotelsAndRooms() {
        Hotel h1 = Hotel.builder()
                .name("InterContinental Landmark72")
                .location("Keangnam Landmark72, Hanoi, Vietnam")
                .description("Spectacular Hanoi city views from the tallest luxury hotel in Vietnam.")
                .rating(new BigDecimal("4.9"))
                .isActive(true)
                .build();

        Hotel h2 = Hotel.builder()
                .name("Sheraton Hanoi Westlake")
                .location("K Nghi Tam, Tay Ho, Hanoi, Vietnam")
                .description("Scenic lakeside luxury resort offering a peaceful escape in Hanoi.")
                .rating(new BigDecimal("4.7"))
                .isActive(true)
                .build();

        Hotel h3 = Hotel.builder()
                .name("Hilton Saigon Riverview")
                .location("Me Linh Square, District 1, Ho Chi Minh City, Vietnam")
                .description("Premium business and leisure hotel overlooking the historic Saigon River.")
                .rating(new BigDecimal("4.6"))
                .isActive(true)
                .build();

        Hotel h4 = Hotel.builder()
                .name("Pullman Danang Beach Resort")
                .location("Vo Nguyen Giap, Ngu Hanh Son, Da Nang, Vietnam")
                .description("Stunning beachfront resort with premium amenities, infinity pool, and white sand beach.")
                .rating(new BigDecimal("4.8"))
                .isActive(true)
                .build();

        List<Hotel> savedHotels = hotelRepository.saveAll(List.of(h1, h2, h3, h4));

        for (Hotel hotel : savedHotels) {
            List<HotelImage> images = new ArrayList<>();
            images.add(HotelImage.builder()
                    .hotel(hotel)
                    .imageUrl("https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800&q=80")
                    .imageFormat("JPEG")
                    .build());
            images.add(HotelImage.builder()
                    .hotel(hotel)
                    .imageUrl("https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800&q=80")
                    .imageFormat("JPEG")
                    .build());
            hotel.setImages(images);

            List<Room> rooms = new ArrayList<>();
            rooms.add(Room.builder()
                    .hotel(hotel)
                    .roomType("Deluxe Executive")
                    .roomNumber(hotel.getHotelId() + "01")
                    .price(new BigDecimal("180.00"))
                    .status("AVAILABLE")
                    .build());
            rooms.add(Room.builder()
                    .hotel(hotel)
                    .roomType("Presidential Suite")
                    .roomNumber(hotel.getHotelId() + "02")
                    .price(new BigDecimal("350.00"))
                    .status("AVAILABLE")
                    .build());
            rooms.add(Room.builder()
                    .hotel(hotel)
                    .roomType("Standard Twin Room")
                    .roomNumber(hotel.getHotelId() + "03")
                    .price(new BigDecimal("110.00"))
                    .status("AVAILABLE")
                    .build());
            hotel.setRooms(rooms);
            hotelRepository.save(hotel);
        }
    }

    private void seedUsers() {
        User admin = User.builder()
                .email("admin@hotelbooking.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .phoneNumber("0901234567")
                .role("ADMIN")
                .status("ACTIVE")
                .build();

        User customer = User.builder()
                .email("customer@gmail.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .fullName("Nguyen Van A")
                .phoneNumber("0987654321")
                .role("CUSTOMER")
                .status("ACTIVE")
                .build();

        userRepository.saveAll(List.of(admin, customer));
    }
}
