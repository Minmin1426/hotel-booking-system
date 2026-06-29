package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.user.User;
import com.stripe.model.checkout.Session;
import org.mockito.MockedStatic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentAuditLogRepository auditLogRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final String stripeKey = "sk_test_mock";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "stripeApiKey", stripeKey);
        ReflectionTestUtils.setField(paymentService, "stripeSuccessUrl", "http://localhost");
        ReflectionTestUtils.setField(paymentService, "stripeCancelUrl", "http://localhost");
    }

    @Test
    void testCreatePaymentRequest_Success() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING_PAYMENT");
        booking.setTotalAmount(BigDecimal.valueOf(1000));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setBookingId(1L);
        requestDTO.setPaymentMethod("STRIPE");

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = new Session();
            mockSession.setId("cs_test_123");
            mockSession.setUrl("https://checkout.stripe.com/pay");
            
            mockedSession.when(() -> Session.create(any(com.stripe.param.checkout.SessionCreateParams.class)))
                    .thenReturn(mockSession);

            PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);

            assertNotNull(response);
            assertEquals("cs_test_123", response.getTransactionId());
            assertEquals("https://checkout.stripe.com/pay", response.getPaymentUrl());
            verify(paymentRepository, times(1)).save(any(Payment.class));
            verify(auditLogRepository, times(1)).save(any());
        }
    }

    @Test
    void testCreatePaymentRequest_NotPending() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("CONFIRMED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setBookingId(1L);

        assertThrows(BusinessException.class, () -> paymentService.createPaymentRequest(requestDTO));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void testVerifyPayment_Success() {
        String transactionId = "cs_test_123";

        User user = new User();
        user.setEmail("test@test.com");

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING_PAYMENT");
        booking.setUser(user);
        booking.setBookingCode("B-12345");

        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setStatus("PROCESSING");
        payment.setBooking(booking);

        when(paymentRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(payment));

        try (MockedStatic<Session> mockedSession = mockStatic(Session.class)) {
            Session mockSession = new Session();
            mockSession.setPaymentStatus("paid");
            
            mockedSession.when(() -> Session.retrieve(transactionId))
                    .thenReturn(mockSession);

            paymentService.verifyPayment(transactionId);

            assertEquals("SUCCESS", payment.getStatus());
            assertEquals("SUCCESS", booking.getPaymentStatus());
            assertEquals("CONFIRMED", booking.getStatus());
            
            verify(paymentRepository, times(1)).save(payment);
            verify(bookingRepository, times(1)).save(booking);
            verify(emailService, times(1)).sendBookingConfirmationEmail("test@test.com", "B-12345");
            verify(auditLogRepository, times(1)).save(any());
        }
    }

    @Test
    void testProcessRefund_Success() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setPaymentStatus("SUCCESS");
        
        User user = new User();
        user.setEmail("test@test.com");
        booking.setUser(user);
        booking.setBookingCode("B-12345");

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setBooking(booking);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_BookingId(1L)).thenReturn(Optional.of(payment));

        paymentService.processRefund(1L);

        assertEquals("SUCCESS", payment.getRefundStatus());
        assertEquals("CANCELLED", booking.getStatus());
        verify(emailService, times(1)).sendRefundConfirmationEmail(eq("test@test.com"), eq("B-12345"), eq(BigDecimal.valueOf(1000)));
        verify(paymentRepository, times(1)).save(payment);
        verify(bookingRepository, times(1)).save(booking);
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void testProcessRefund_NotPaid() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setPaymentStatus("FAILED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BusinessException exception = assertThrows(BusinessException.class, () -> paymentService.processRefund(1L));
        assertEquals("Cannot refund a booking that has not been paid.", exception.getMessage());
    }

    @Test
    void testRetryFailedRefunds() {
        Payment payment = new Payment();
        payment.setRefundStatus("FAILED");
        payment.setRefundRetryCount(1);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setRefundTransactionId("txn-1");

        Booking booking = new Booking();
        User user = new User();
        user.setEmail("user@test.com");
        booking.setUser(user);
        booking.setBookingCode("B-999");
        payment.setBooking(booking);

        when(paymentRepository.findByRefundStatusAndRefundRetryCountLessThan("FAILED", 3))
                .thenReturn(java.util.Collections.singletonList(payment));

        paymentService.retryFailedRefunds();

        assertEquals(2, payment.getRefundRetryCount());
        assertEquals("SUCCESS", payment.getRefundStatus());
        assertEquals("CANCELLED", booking.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }
}
