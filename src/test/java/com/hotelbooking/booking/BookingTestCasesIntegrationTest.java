package com.hotelbooking.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.booking.dto.AdminCreateBookingRequest;
import com.hotelbooking.booking.dto.AdminUpdateBookingRequest;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.DateValidationRequest;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelRepository;
import com.hotelbooking.payment.Payment;
import com.hotelbooking.payment.PaymentRepository;
import com.hotelbooking.payment.dto.PaymentConfirmRequest;
import com.hotelbooking.room.Room;
import com.hotelbooking.room.RoomLock;
import com.hotelbooking.room.RoomLockCleanupScheduler;
import com.hotelbooking.room.RoomLockRepository;
import com.hotelbooking.room.RoomRepository;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.voucher.Voucher;
import com.hotelbooking.voucher.VoucherRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingTestCasesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomLockRepository roomLockRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RoomLockCleanupScheduler roomLockCleanupScheduler;

    private User customer;
    private User admin;
    private User receptionist;
    private Hotel hotel;
    private Room room101;
    private Room room102;
    private Voucher voucher;

    private String customerToken;
    private String adminToken;
    private String receptionistToken;

    @BeforeEach
    void setUp() {
        cleanupDatabase();

        // Create Users
        customer = User.builder()
                .email("cust@example.com")
                .fullName("Customer User")
                .role("CUSTOMER")
                .status("ACTIVE")
                .passwordHash("password")
                .build();
        customer = userRepository.save(customer);

        admin = User.builder()
                .email("admin@example.com")
                .fullName("Admin User")
                .role("ADMIN")
                .status("ACTIVE")
                .passwordHash("password")
                .build();
        admin = userRepository.save(admin);

        receptionist = User.builder()
                .email("recep@example.com")
                .fullName("Receptionist User")
                .role("RECEPTIONIST")
                .status("ACTIVE")
                .passwordHash("password")
                .build();
        receptionist = userRepository.save(receptionist);

        // Generate tokens
        customerToken = "Bearer " + jwtService.generateAccessToken(customer.getEmail(), customer.getUserId(), customer.getRole());
        adminToken = "Bearer " + jwtService.generateAccessToken(admin.getEmail(), admin.getUserId(), admin.getRole());
        receptionistToken = "Bearer " + jwtService.generateAccessToken(receptionist.getEmail(), receptionist.getUserId(), receptionist.getRole());

        // Create Hotel
        hotel = Hotel.builder()
                .name("Grand Palace")
                .location("Hanoi")
                .isActive(true)
                .build();
        hotel = hotelRepository.save(hotel);

        // Create Rooms
        room101 = Room.builder()
                .hotel(hotel)
                .roomType("Deluxe")
                .price(BigDecimal.valueOf(100.00))
                .roomNumber("101")
                .status("AVAILABLE")
                .build();
        room101 = roomRepository.save(room101);

        room102 = Room.builder()
                .hotel(hotel)
                .roomType("Suite")
                .price(BigDecimal.valueOf(200.00))
                .roomNumber("102")
                .status("AVAILABLE")
                .build();
        room102 = roomRepository.save(room102);

        // Create Voucher
        voucher = Voucher.builder()
                .code("PROMO10")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(10))
                .minBookingValue(BigDecimal.valueOf(50))
                .maxDiscount(BigDecimal.valueOf(50.00))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .maxUsage(100)
                .currentUsage(0)
                .build();
        voucher = voucherRepository.save(voucher);
    }

    @AfterEach
    void tearDown() {
        cleanupDatabase();
    }

    private void cleanupDatabase() {
        roomLockRepository.deleteAll();
        paymentRepository.deleteAll();
        bookingRepository.deleteAll();
        roomRepository.deleteAll();
        hotelRepository.deleteAll();
        userRepository.deleteAll();
        voucherRepository.deleteAll();
    }

    // ==========================================
    // INTEGRATION TESTS (TC-IT-002 to TC-IT-014)
    // ==========================================

    @Test
    void test_TC_IT_002_createBooking_success_dbAndRoomLock() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(3);

        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .paymentMethod("ONLINE")
                .adults(2)
                .children(0)
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(345.00)); // (100*3 + 0 surcharge) * 1.15 = 345

        List<Booking> bookings = bookingRepository.findAll();
        assertEquals(1, bookings.size());
        assertEquals("PENDING", bookings.get(0).getStatus());

        List<RoomLock> locks = roomLockRepository.findAll();
        assertEquals(1, locks.size());
        assertEquals(room101.getRoomId(), locks.get(0).getRoom().getRoomId());
    }

    @Test
    void test_TC_IT_003_createBooking_fails_overlapConfirmed() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        // Pre-create and confirm a booking
        Booking booking = Booking.builder()
                .bookingCode("BK-CONFIRMED")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        BookingRoom br = BookingRoom.builder()
                .booking(booking)
                .room(room101)
                .quantity(1)
                .priceAtBooking(room101.getPrice())
                .build();
        booking.setBookingRooms(List.of(br));
        bookingRepository.save(booking);

        // Try booking same room on overlapping dates
        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .paymentMethod("ONLINE")
                .adults(2)
                .children(0)
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("is already locked or booked for the selected dates")));
    }

    @Test
    void test_TC_IT_004_createBooking_fails_overlapActiveRoomLock() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        // Pre-create booking & room lock
        Booking booking = Booking.builder()
                .bookingCode("BK-LOCKED")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        RoomLock lock = RoomLock.builder()
                .room(room101)
                .booking(booking)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        roomLockRepository.save(lock);

        // Try booking overlapping room
        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .paymentMethod("ONLINE")
                .adults(2)
                .children(0)
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("is already locked or booked for the selected dates")));
    }

    @Test
    void test_TC_IT_005_renewRoomLock_success() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-RENEW")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        LocalDateTime originalExpires = LocalDateTime.now().plusMinutes(10);
        RoomLock lock = RoomLock.builder()
                .room(room101)
                .booking(booking)
                .expiresAt(originalExpires)
                .build();
        lock = roomLockRepository.save(lock);

        mockMvc.perform(put("/api/v1/bookings/" + booking.getBookingId() + "/lock/renew")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        RoomLock updatedLock = roomLockRepository.findById(lock.getLockId()).orElseThrow();
        assertTrue(updatedLock.getExpiresAt().isAfter(originalExpires));
    }

    @Test
    @Transactional
    void test_TC_IT_006_schedulerCleansExpiredLocks() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-EXPIRED-SCHED")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        RoomLock lock = RoomLock.builder()
                .room(room101)
                .booking(booking)
                .expiresAt(LocalDateTime.now().minusMinutes(5)) // Expired
                .build();
        roomLockRepository.save(lock);

        roomLockCleanupScheduler.cleanupExpiredRoomLocks();

        List<RoomLock> locks = roomLockRepository.findAll();
        assertTrue(locks.isEmpty());

        Booking updatedBooking = bookingRepository.findById(booking.getBookingId()).orElseThrow();
        assertEquals("FAILED", updatedBooking.getStatus());
    }

    @Test
    void test_TC_IT_007_confirmBooking_success() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-CONFIRM-IT")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("ONLINE")
                .amount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        paymentRepository.save(payment);

        RoomLock lock = RoomLock.builder()
                .room(room101)
                .booking(booking)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        roomLockRepository.save(lock);

        PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                .bookingCode("BK-CONFIRM-IT")
                .transactionId("TXN-12345")
                .amount(BigDecimal.valueOf(300))
                .paymentMethod("ONLINE")
                .build();

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingStatus").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.paymentStatus").value("SUCCESS"));

        Booking confirmedBooking = bookingRepository.findById(booking.getBookingId()).orElseThrow();
        assertEquals("CONFIRMED", confirmedBooking.getStatus());

        List<RoomLock> locks = roomLockRepository.findAll();
        assertTrue(locks.isEmpty());
    }

    @Test
    void test_TC_IT_008_myHistory_success() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-HIST")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        bookingRepository.save(booking);

        mockMvc.perform(get("/api/v1/bookings/my-history?page=0&size=10")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void test_TC_IT_009_preventDuplicatePayment() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-DUP")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("ONLINE")
                .amount(BigDecimal.valueOf(300))
                .status("SUCCESS")
                .transactionId("TXN-999")
                .build();
        paymentRepository.save(payment);

        PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                .bookingCode("BK-DUP")
                .transactionId("TXN-999")
                .amount(BigDecimal.valueOf(300))
                .paymentMethod("ONLINE")
                .build();

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Transaction ID đã được xử lý")));
    }

    @Test
    void test_TC_IT_010_cancellationUpdatesPaymentStatus() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-CANCEL-IT")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("ONLINE")
                .amount(BigDecimal.valueOf(300))
                .status("SUCCESS")
                .build();
        paymentRepository.save(payment);

        mockMvc.perform(post("/api/v1/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.data.refundStatus").value("REFUND_PENDING"));

        List<Payment> payments = paymentRepository.findByBookingBookingId(booking.getBookingId());
        assertEquals("REFUND_PENDING", payments.get(0).getStatus());
    }

    @Test
    void test_TC_IT_011_receptionistCreatesCounterBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        AdminCreateBookingRequest request = AdminCreateBookingRequest.builder()
                .userId(customer.getUserId())
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .paymentMethod("CASH")
                .build();

        mockMvc.perform(post("/api/v1/admin/bookings")
                        .header("Authorization", receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        List<RoomLock> locks = roomLockRepository.findAll();
        assertEquals(1, locks.size());
    }

    @Test
    void test_TC_IT_012_receptionistConfirmsCashBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-CASH")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("CASH")
                .amount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        paymentRepository.save(payment);

        UpdateBookingStatusRequest statusRequest = new UpdateBookingStatusRequest("CONFIRMED");

        mockMvc.perform(patch("/api/v1/admin/bookings/" + booking.getBookingId() + "/status")
                        .header("Authorization", receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.data.paymentStatus").value("COMPLETED"));
    }

    @Test
    void test_TC_IT_013_getBookingDetails() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-DETAIL")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(get("/api/v1/bookings/" + booking.getBookingId())
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.bookingCode").value("BK-DETAIL"));
    }

    @Test
    void test_TC_IT_014_adminUpdatesBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-UPDATE-AD")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        RoomLock originalLock = RoomLock.builder()
                .room(room101)
                .booking(booking)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .build();
        roomLockRepository.save(originalLock);

        // 1. Verify that attempting to change roomIds throws a bad request exception
        AdminUpdateBookingRequest updateRoomRequest = AdminUpdateBookingRequest.builder()
                .roomIds(List.of(room102.getRoomId()))
                .build();

        mockMvc.perform(put("/api/v1/admin/bookings/" + booking.getBookingId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRoomRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Chỉnh sửa phòng không được phép. Vui lòng hủy/xóa đơn cũ và tạo đơn mới."));

        // 2. Verify that attempting to change checkInDate throws a bad request exception
        AdminUpdateBookingRequest updateCheckInRequest = AdminUpdateBookingRequest.builder()
                .checkInDate(checkIn.plusDays(1))
                .build();

        mockMvc.perform(put("/api/v1/admin/bookings/" + booking.getBookingId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCheckInRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Chỉnh sửa ngày check-in không được phép. Vui lòng hủy/xóa đơn cũ và tạo đơn mới."));

        // 3. Verify that updating payment status with a reason works successfully (allowed override)
        AdminUpdateBookingRequest validUpdateRequest = AdminUpdateBookingRequest.builder()
                .paymentStatus("SUCCESS")
                .paymentUpdateReason("Guest paid cash at reception counter")
                .build();

        mockMvc.perform(put("/api/v1/admin/bookings/" + booking.getBookingId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CONFIRMED"));

        Booking updatedBooking = bookingRepository.findById(booking.getBookingId()).orElseThrow();
        assertEquals("CONFIRMED", updatedBooking.getStatus());
        assertEquals("SUCCESS", updatedBooking.getPaymentStatus());
    }

    // ==========================================
    // SYSTEM TESTS (TC-ST-001 to TC-ST-006)
    // ==========================================

    @Test
    void test_TC_ST_001_concurrencyBookingSameRoom() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .paymentMethod("ONLINE")
                .adults(2)
                .children(0)
                .build();

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executor.submit(() -> {
                try {
                    latch.await(); // wait for signal to start together
                    var response = mockMvc.perform(post("/api/v1/bookings")
                                    .header("Authorization", customerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                            .andReturn().getResponse();

                    if (response.getStatus() == 201) {
                        successCount.incrementAndGet();
                    } else if (response.getStatus() == 400) {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        latch.countDown(); // start threads
        doneLatch.await(); // wait for all threads to finish

        assertEquals(1, successCount.get(), "Only one thread should successfully create booking");
        assertEquals(1, failureCount.get(), "The other thread must fail due to lock concurrency exception");
    }

    @Test
    void test_TC_ST_002_springSecurityBlocksAnonymous() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        LocalDate checkOut = checkIn.plusDays(3);

        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        // No Authorization header
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_TC_ST_003_springSecurityBlocksCustomerFromAdminEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/admin/bookings")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_TC_ST_004_globalExceptionHandlerJsonFormat() throws Exception {
        LocalDate checkIn = LocalDate.now().minusDays(1); // past date
        LocalDate checkOut = checkIn.plusDays(3);

        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .adults(2)
                .children(0)
                .build();

        mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("Check-in date cannot be in the past")))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    // ==========================================
    // ACCEPTANCE TESTS (TC-AT-001 to TC-AT-012)
    // ==========================================

    @Test
    void test_TC_AT_001_e2e_happyPath() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        // Step 1: Create booking
        BookingRequest request = BookingRequest.builder()
                .hotelId(hotel.getHotelId())
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .roomIds(List.of(room101.getRoomId()))
                .paymentMethod("ONLINE")
                .adults(2)
                .children(0)
                .build();

        String responseContent = mockMvc.perform(post("/api/v1/bookings")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String bookingCode = objectMapper.readTree(responseContent).get("data").get("bookingCode").asText();

        // Step 2: Confirm Payment
        PaymentConfirmRequest confirmRequest = PaymentConfirmRequest.builder()
                .bookingCode(bookingCode)
                .transactionId("TXN-E2E-1")
                .amount(BigDecimal.valueOf(345.00))
                .paymentMethod("ONLINE")
                .build();

        mockMvc.perform(post("/api/v1/bookings/confirm")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bookingStatus").value("CONFIRMED"));

        // Step 3: Verify booking confirmed and room lock released
        List<Booking> bookings = bookingRepository.findAll();
        assertEquals("CONFIRMED", bookings.get(0).getStatus());
        assertTrue(roomLockRepository.findAll().isEmpty());
    }

    @Test
    void test_TC_AT_003_blockCancelOtherUsersBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        // Create booking for customer B
        User customerB = User.builder()
                .email("cust_b@example.com")
                .fullName("Customer B")
                .role("CUSTOMER")
                .status("ACTIVE")
                .passwordHash("password")
                .build();
        customerB = userRepository.save(customerB);

        Booking booking = Booking.builder()
                .bookingCode("BK-B")
                .user(customerB)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", customerToken) // Customer A token
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Access denied: You do not own this booking")));
    }

    @Test
    void test_TC_AT_004_blockCancelBookingAfterCheckIn() throws Exception {
        LocalDate checkIn = LocalDate.now().minusDays(1); // yesterday
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-PAST")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Cannot cancel booking after check-in date")));
    }

    @Test
    void test_TC_AT_008_adminDeletesBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-DELETE-AD")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(delete("/api/v1/admin/bookings/" + booking.getBookingId())
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(containsString("Booking deleted successfully")));

        assertFalse(bookingRepository.findById(booking.getBookingId()).isPresent());
    }

    @Test
    void test_TC_AT_009_receptionistBlockedFromDeleteBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-DELETE-REC")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CONFIRMED")
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(delete("/api/v1/admin/bookings/" + booking.getBookingId())
                        .header("Authorization", receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void test_TC_AT_010_blockCancelAlreadyCancelledBooking() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-CANCELLED-ALREADY")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CANCELLED")
                .build();
        booking = bookingRepository.save(booking);

        mockMvc.perform(post("/api/v1/bookings/" + booking.getBookingId() + "/cancel")
                        .header("Authorization", customerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Booking is already cancelled")));
    }

    @Test
    void test_TC_Checkout_Success() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Booking booking = Booking.builder()
                .bookingCode("BK-CHECKOUT-TEST-NEW")
                .user(customer)
                .hotel(hotel)
                .checkInDate(checkIn.atStartOfDay())
                .checkOutDate(checkOut.atStartOfDay())
                .totalAmount(BigDecimal.valueOf(300))
                .status("CHECKED_IN")
                .build();
        booking = bookingRepository.save(booking);

        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod("CASH")
                .amount(BigDecimal.valueOf(300))
                .status("PENDING")
                .build();
        paymentRepository.save(payment);

        BookingRoom br = BookingRoom.builder()
                .booking(booking)
                .room(room101)
                .quantity(1)
                .priceAtBooking(room101.getPrice())
                .build();
        booking.setBookingRooms(List.of(br));
        bookingRepository.save(booking);

        // Make sure room starts as AVAILABLE
        room101.setStatus("AVAILABLE");
        roomRepository.save(room101);

        UpdateBookingStatusRequest statusRequest = new UpdateBookingStatusRequest("CHECKED_OUT");

        mockMvc.perform(patch("/api/v1/admin/bookings/" + booking.getBookingId() + "/status")
                        .header("Authorization", receptionistToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("CHECKED_OUT"));

        // Verify room status is updated to UNAVAILABLE
        Room updatedRoom = roomRepository.findById(room101.getRoomId()).orElseThrow();
        assertEquals("UNAVAILABLE", updatedRoom.getStatus());
    }
}
