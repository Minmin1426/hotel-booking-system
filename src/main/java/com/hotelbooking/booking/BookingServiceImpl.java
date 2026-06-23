package com.hotelbooking.booking;
import com.hotelbooking.booking.dto.AdminBookingResponse;
import com.hotelbooking.booking.dto.BookingConfirmResponse;
import com.hotelbooking.booking.dto.BookingHistoryResponse;
import com.hotelbooking.booking.dto.BookingRequest;
import com.hotelbooking.booking.dto.BookingResponse;
import com.hotelbooking.booking.dto.CancelBookingResponse;
import com.hotelbooking.booking.dto.DateValidationResponse;
import com.hotelbooking.booking.dto.UpdateBookingStatusRequest;
import com.hotelbooking.common.dto.PagedResponse;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.hotel.Hotel;
import com.hotelbooking.hotel.HotelRepository;
import com.hotelbooking.payment.Payment;
import com.hotelbooking.payment.PaymentRepository;
import com.hotelbooking.payment.dto.PaymentConfirmRequest;
import com.hotelbooking.room.Room;
import com.hotelbooking.room.RoomLock;
import com.hotelbooking.room.RoomLockRepository;
import com.hotelbooking.room.RoomLockService;
import com.hotelbooking.room.RoomRepository;
import com.hotelbooking.setting.SystemSettingService;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final int MAX_PAGE_SIZE = 20;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;
    private final RoomLockService roomLockService;
    private final RoomLockRepository roomLockRepository;
    private final PaymentRepository paymentRepository;
    private final SystemSettingService systemSettingService;

    @Override
    @Transactional(readOnly = true)
    public DateValidationResponse validateDates(LocalDate checkInDate, LocalDate checkOutDate) {
        log.info("Validating dates: checkIn={}, checkOut={}", checkInDate, checkOutDate);
        LocalDate today = LocalDate.now();

        if (checkInDate == null || checkOutDate == null) {
            return DateValidationResponse.builder()
                    .valid(false)
                    .nights(0)
                    .message("Check-in and check-out dates cannot be null")
                    .build();
        }

        if (checkInDate.isBefore(today)) {
            return DateValidationResponse.builder()
                    .valid(false)
                    .nights(0)
                    .message("Check-in date cannot be in the past")
                    .build();
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            return DateValidationResponse.builder()
                    .valid(false)
                    .nights(0)
                    .message("Check-out date must be after check-in date")
                    .build();
        }

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights < 1) {
            return DateValidationResponse.builder()
                    .valid(false)
                    .nights(0)
                    .message("Minimum stays is 1 night")
                    .build();
        }

        return DateValidationResponse.builder()
                .valid(true)
                .nights(nights)
                .message("Dates are valid")
                .build();
    }

    @Override
    public BookingResponse createBooking(BookingRequest request, String currentUserEmail) {
        log.info("Creating booking for user: {} at hotel ID: {}", currentUserEmail, request.getHotelId());

        // Validate stay period dates
        DateValidationResponse validation = validateDates(request.getCheckInDate(), request.getCheckOutDate());
        if (!validation.isValid()) {
            throw new BusinessException(validation.getMessage());
        }

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + request.getHotelId()));

        List<Room> rooms = new ArrayList<>();
        BigDecimal totalRoomPricePerNight = BigDecimal.ZERO;

        for (Long roomId : request.getRoomIds()) {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new ResourceNotFoundException("Room not found with ID: " + roomId));

            if (!room.getHotel().getHotelId().equals(hotel.getHotelId())) {
                throw new BusinessException(String.format("Room %s does not belong to the selected hotel %s", 
                        room.getRoomNumber(), hotel.getName()));
            }
            rooms.add(room);
            totalRoomPricePerNight = totalRoomPricePerNight.add(room.getPrice());
        }

        long nights = validation.getNights();
        BigDecimal totalAmount = totalRoomPricePerNight.multiply(BigDecimal.valueOf(nights));

        // Generate booking code
        String bookingCode = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .user(user)
                .hotel(hotel)
                .checkInDate(request.getCheckInDate().atStartOfDay())
                .checkOutDate(request.getCheckOutDate().atStartOfDay())
                .totalAmount(totalAmount)
                .status("PENDING")
                .build();

        booking = bookingRepository.save(booking);

        List<BookingRoom> bookingRooms = new ArrayList<>();
        for (Room room : rooms) {
            BookingRoom br = BookingRoom.builder()
                    .booking(booking)
                    .room(room)
                    .quantity(1)
                    .priceAtBooking(room.getPrice())
                    .build();
            bookingRooms.add(br);
        }
        booking.setBookingRooms(bookingRooms);
        booking = bookingRepository.save(booking);

        // Seed a pending payment
        String method = request.getPaymentMethod() != null ? request.getPaymentMethod() : "ONLINE";
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentMethod(method)
                .amount(totalAmount)
                .status("PENDING")
                .build();
        paymentRepository.save(payment);

        // Lock rooms
        List<RoomLock> locks = roomLockService.lockRoomsForBooking(booking, request.getRoomIds());
        LocalDateTime lockExpiresAt = locks.isEmpty() ? LocalDateTime.now().plusMinutes(systemSettingService.getLockDurationMinutes()) : locks.get(0).getExpiresAt();

        return mapToResponse(booking, lockExpiresAt);
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        log.info("Confirming booking ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Booking is not in PENDING status and cannot be confirmed");
        }

        // Validate availability
        for (BookingRoom br : booking.getBookingRooms()) {
            Long roomId = br.getRoom().getRoomId();
            
            var confirmed = bookingRepository.findConfirmedBookingsOverlapping(roomId, booking.getCheckInDate(), booking.getCheckOutDate());
            if (!confirmed.isEmpty()) {
                throw new BusinessException(String.format("Room %s is already booked for the selected dates", br.getRoom().getRoomNumber()));
            }
            
            var activeLocks = roomLockRepository.findActiveLocksOverlapping(roomId, booking.getCheckInDate(), booking.getCheckOutDate(), LocalDateTime.now());
            for (RoomLock otherLock : activeLocks) {
                if (!otherLock.getBooking().getBookingId().equals(bookingId)) {
                    throw new BusinessException(String.format("Room %s is locked by another transaction", br.getRoom().getRoomNumber()));
                }
            }
        }

        booking.setStatus("CONFIRMED");
        booking.setConfirmedAt(LocalDateTime.now());
        Booking savedBooking = bookingRepository.save(booking);

        // Release the temporary locks
        roomLockService.releaseLocksForBooking(bookingId);

        return mapToResponse(savedBooking, null);
    }

    @Override
    public BookingResponse failBooking(Long bookingId) {
        log.info("Failing/cancelling booking ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Booking is not in PENDING status and cannot be marked as failed");
        }

        booking.setStatus("FAILED");
        Booking savedBooking = bookingRepository.save(booking);

        // Release the locks
        roomLockService.releaseLocksForBooking(bookingId);

        // Update payment to failed
        List<Payment> payments = paymentRepository.findByBookingBookingId(bookingId);
        for (Payment p : payments) {
            if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                p.setStatus("FAILED");
                paymentRepository.save(p);
            }
        }

        return mapToResponse(savedBooking, null);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        List<RoomLock> locks = roomLockRepository.findByBookingBookingId(bookingId);
        LocalDateTime lockExpiresAt = locks.isEmpty() ? null : locks.get(0).getExpiresAt();

        return mapToResponse(booking, lockExpiresAt);
    }

    @Override
    public void renewLock(Long bookingId) {
        log.info("Service: Renewing room locks for booking ID: {}", bookingId);
        roomLockService.renewLocksForBooking(bookingId);
    }

    // UC-12: Auto-confirm booking sau khi online payment thành công
    @Override
    public BookingConfirmResponse confirmBooking(PaymentConfirmRequest request) {
        log.info("UC-12: Confirming booking for bookingCode={}, transactionId={}",
                request.getBookingCode(), request.getTransactionId());

        if (request.getTransactionId() != null && paymentRepository.existsByTransactionId(request.getTransactionId())) {
            log.warn("UC-12: Duplicate transaction detected: {}", request.getTransactionId());
            throw new BusinessException("Transaction ID đã được xử lý: " + request.getTransactionId());
        }

        Booking booking = bookingRepository.findByBookingCode(request.getBookingCode())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy booking: " + request.getBookingCode()));

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Booking không ở trạng thái PENDING. Trạng thái hiện tại: " + booking.getStatus());
        }

        List<Payment> payments = paymentRepository.findByBookingBookingId(booking.getBookingId());
        Payment payment;
        if (!payments.isEmpty()) {
            payment = payments.get(0);
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setAmount(request.getAmount());
            payment.setStatus("SUCCESS");
            payment.setTransactionId(request.getTransactionId());
        } else {
            payment = Payment.builder()
                    .booking(booking)
                    .paymentMethod(request.getPaymentMethod())
                    .amount(request.getAmount())
                    .status("SUCCESS")
                    .transactionId(request.getTransactionId())
                    .build();
        }
        paymentRepository.save(payment);

        LocalDateTime now = LocalDateTime.now();
        booking.setStatus("CONFIRMED");
        booking.setConfirmedAt(now);
        bookingRepository.save(booking);

        log.info("UC-12: Booking {} confirmed at {}", booking.getBookingCode(), now);

        try {
            roomLockService.releaseLocksForBooking(booking.getBookingId());
        } catch (Exception e) {
            log.error("Failed to release room locks: {}", e.getMessage());
        }

        return BookingConfirmResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .bookingStatus("CONFIRMED")
                .paymentStatus("SUCCESS")
                .transactionId(request.getTransactionId())
                .totalAmount(booking.getTotalAmount())
                .confirmedAt(now)
                .hotelName(booking.getHotel().getName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .build();
    }

    // UC-15: Lấy lịch sử đặt phòng của user hiện tại
    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BookingHistoryResponse> getBookingHistory(Long userId, int page, int size) {
        log.info("UC-15: Getting booking history for userId={}, page={}, size={}", userId, page, size);

        int validatedSize = Math.min(size, MAX_PAGE_SIZE);
        if (validatedSize <= 0) validatedSize = MAX_PAGE_SIZE;

        Pageable pageable = PageRequest.of(page, validatedSize);
        Page<Booking> bookingPage = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<BookingHistoryResponse> content = bookingPage.getContent().stream()
                .map(this::toHistoryResponse)
                .collect(Collectors.toList());

        return PagedResponse.<BookingHistoryResponse>builder()
                .content(content)
                .page(bookingPage.getNumber())
                .size(bookingPage.getSize())
                .totalElements(bookingPage.getTotalElements())
                .totalPages(bookingPage.getTotalPages())
                .first(bookingPage.isFirst())
                .last(bookingPage.isLast())
                .build();
    }

    // UC-22: Admin processing manual/offline bookings
    @Override
    public AdminBookingResponse processBooking(Long bookingId, UpdateBookingStatusRequest request) {
        log.info("UC-22: Processing bookingId={} with target status={}", bookingId, request.status());

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));

        List<Payment> payments = paymentRepository.findByBookingBookingId(bookingId);
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("Payment record not found for booking ID: " + bookingId);
        }
        Payment payment = payments.get(0);

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Only pending bookings can be processed");
        }

        String paymentMethod = payment.getPaymentMethod().toUpperCase();
        if (!"CASH".equals(paymentMethod) && !"BANK_TRANSFER".equals(paymentMethod)) {
            throw new BusinessException("Only manual/offline bookings can be processed manually by admin");
        }

        String targetStatus = request.status().toUpperCase();
        String targetPaymentStatus = payment.getStatus();

        if ("CONFIRMED".equals(targetStatus)) {
            booking.setStatus("CONFIRMED");
            booking.setConfirmedAt(LocalDateTime.now());
            targetPaymentStatus = "COMPLETED";
        } else if ("CANCELLED".equals(targetStatus)) {
            booking.setStatus("CANCELLED");
            targetPaymentStatus = "FAILED";
        }

        payment.setStatus(targetPaymentStatus);
        paymentRepository.save(payment);
        bookingRepository.save(booking);

        try {
            roomLockService.releaseLocksForBooking(bookingId);
        } catch (Exception e) {
            log.error("Failed to release locks: {}", e.getMessage());
        }

        return new AdminBookingResponse(
                booking.getBookingId(),
                booking.getBookingCode(),
                booking.getUser().getEmail(),
                booking.getHotel().getName(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalAmount(),
                booking.getStatus(),
                payment.getPaymentMethod(),
                payment.getStatus(),
                booking.getUpdatedAt()
        );
    }

    // UC-14: Hủy đặt phòng
    @Override
    public CancelBookingResponse cancelBooking(Long bookingId, Long customerId) {
        log.info("UC-14: Cancelling booking ID: {} for customer ID: {}", bookingId, customerId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with ID: " + bookingId));
                
        if (!booking.getUser().getUserId().equals(customerId)) {
            throw new BusinessException("Access denied: You do not own this booking");
        }
        
        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Booking is already cancelled");
        }
        if ("COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("Completed booking cannot be cancelled");
        }
        
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
        
        try {
            roomLockService.releaseLocksForBooking(bookingId);
        } catch (Exception e) {
            log.error("Failed to release locks: {}", e.getMessage());
        }
        
        List<Payment> payments = paymentRepository.findByBookingBookingId(bookingId);
        String refundStatus = "NO_PAYMENT";
        if (!payments.isEmpty()) {
            refundStatus = "REFUND_PENDING";
            for (Payment p : payments) {
                if ("SUCCESS".equalsIgnoreCase(p.getStatus()) || "COMPLETED".equalsIgnoreCase(p.getStatus())) {
                    p.setStatus("REFUND_PENDING");
                    paymentRepository.save(p);
                } else if ("PENDING".equalsIgnoreCase(p.getStatus())) {
                    p.setStatus("FAILED");
                    paymentRepository.save(p);
                }
            }
        }
        
        return new CancelBookingResponse(
                bookingId,
                "CANCELLED",
                refundStatus,
                "Booking cancelled successfully"
        );
    }

    private BookingResponse mapToResponse(Booking booking, LocalDateTime lockExpiresAt) {
        List<Long> roomIds = booking.getBookingRooms().stream()
                .map(br -> br.getRoom().getRoomId())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUser().getUserId())
                .hotelId(booking.getHotel().getHotelId())
                .checkInDate(booking.getCheckInDate().toLocalDate())
                .checkOutDate(booking.getCheckOutDate().toLocalDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .roomIds(roomIds)
                .lockExpiresAt(lockExpiresAt)
                .build();
    }

    private BookingHistoryResponse toHistoryResponse(Booking booking) {
        return BookingHistoryResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .hotelName(booking.getHotel().getName())
                .hotelLocation(booking.getHotel().getLocation())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .confirmedAt(booking.getConfirmedAt())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdminBookingResponse> getAllBookings(int page, int size, String status, String paymentMethod, String search) {
        log.info("Retrieving all bookings for Admin with filters, page={}, size={}, status={}, paymentMethod={}, search={}", page, size, status, paymentMethod, search);
        
        String statusParam = (status == null || status.trim().isEmpty() || "ALL".equalsIgnoreCase(status.trim())) ? null : status.trim();
        String paymentMethodParam = (paymentMethod == null || paymentMethod.trim().isEmpty() || "ALL".equalsIgnoreCase(paymentMethod.trim())) ? null : paymentMethod.trim();
        String searchParam = (search == null || search.trim().isEmpty()) ? null : search.trim();

        org.springframework.data.domain.Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("createdAt").descending());
        Page<Booking> bookingsPage = bookingRepository.findAllWithFilters(statusParam, paymentMethodParam, searchParam, pageable);
        
        List<AdminBookingResponse> list = bookingsPage.getContent().stream()
                .map(this::mapToAdminBookingResponse)
                .collect(Collectors.toList());
                
        return PagedResponse.<AdminBookingResponse>builder()
                .content(list)
                .page(bookingsPage.getNumber())
                .size(bookingsPage.getSize())
                .totalElements(bookingsPage.getTotalElements())
                .totalPages(bookingsPage.getTotalPages())
                .first(bookingsPage.isFirst())
                .last(bookingsPage.isLast())
                .build();
    }

    private AdminBookingResponse mapToAdminBookingResponse(Booking booking) {
        List<Payment> payments = paymentRepository.findByBookingBookingId(booking.getBookingId());
        String paymentMethod = payments.isEmpty() ? "UNKNOWN" : payments.get(0).getPaymentMethod();
        String paymentStatus = payments.isEmpty() ? "UNKNOWN" : payments.get(0).getStatus();
        return new AdminBookingResponse(
                booking.getBookingId(),
                booking.getBookingCode(),
                booking.getUser().getEmail(),
                booking.getHotel().getName(),
                booking.getCheckInDate(),
                booking.getCheckOutDate(),
                booking.getTotalAmount(),
                booking.getStatus(),
                paymentMethod,
                paymentStatus,
                booking.getUpdatedAt()
        );
    }
}
