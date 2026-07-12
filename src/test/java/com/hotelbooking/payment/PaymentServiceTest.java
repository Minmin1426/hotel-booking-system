package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.user.User;
import com.hotelbooking.voucher.VoucherRepository;
import com.stripe.model.PaymentIntent;
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

    @Mock
    private VoucherRepository voucherRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final String stripeKey = "sk_test_mock";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "stripeApiKey", stripeKey);
        ReflectionTestUtils.setField(paymentService, "stripeWebhookSecret", "whsec_test");
        paymentService.init();
    }

    @Test
    void testCreatePaymentRequest_Success() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setBookingId(1L);
        requestDTO.setPaymentMethod("STRIPE");

        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setId("cs_test_123");
            mockIntent.setClientSecret("secret_test_123");
            
            mockedIntent.when(() -> PaymentIntent.create(any(com.stripe.param.PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);

            assertNotNull(response);
            assertEquals("cs_test_123", response.getTransactionId());
            assertEquals("secret_test_123", response.getClientSecret());
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
        booking.setStatus("PENDING");
        booking.setUser(user);
        booking.setBookingCode("B-12345");

        Payment payment = new Payment();
        payment.setTransactionId(transactionId);
        payment.setStatus("PENDING");
        payment.setBooking(booking);

        when(paymentRepository.findByTransactionId(transactionId)).thenReturn(Optional.of(payment));
        when(paymentRepository.findByTransactionIdForUpdate(transactionId)).thenReturn(Optional.of(payment));

        try (MockedStatic<PaymentIntent> mockedIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = new PaymentIntent();
            mockIntent.setStatus("succeeded");
            
            mockedIntent.when(() -> PaymentIntent.retrieve(transactionId))
                    .thenReturn(mockIntent);

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
        
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setStatus("SUCCESS");
        payment.setBooking(booking);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_BookingId(1L)).thenReturn(Optional.of(payment));

        paymentService.processRefund(1L);

        assertEquals("REFUND_PENDING", payment.getStatus());
        verify(paymentRepository, times(1)).save(payment);
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
        payment.setStatus("REFUND_PENDING");
        payment.setRefundRetryCount(1);
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setRefundTransactionId("txn-1");

        Booking booking = new Booking();
        User user = new User();
        user.setEmail("user@test.com");
        booking.setUser(user);
        booking.setBookingCode("B-999");
        payment.setBooking(booking);

        when(paymentRepository.findByStatusAndRefundRetryCountLessThan("REFUND_PENDING", 3))
                .thenReturn(java.util.Collections.singletonList(payment));

        paymentService.retryFailedRefunds();

        assertEquals(2, payment.getRefundRetryCount());
        assertEquals("REFUNDED", payment.getStatus());
        assertEquals("CANCELLED", booking.getStatus());
        verify(paymentRepository, times(1)).save(payment);
    }
    
    @Test
    void testCreatePaymentRequest_Cash_Success() {
        Booking booking = new Booking();
        booking.setBookingId(2L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(500));

        when(bookingRepository.findById(2L)).thenReturn(Optional.of(booking));

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setBookingId(2L);
        requestDTO.setPaymentMethod("CASH");

        PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);

        assertNotNull(response);
        assertEquals("CASH_PAYMENT", response.getClientSecret());
        assertEquals("PENDING", booking.getPaymentStatus());
        assertEquals("CONFIRMED", booking.getStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(auditLogRepository, times(1)).save(any(PaymentAuditLog.class));
    }

    @Test
    void testConfirmCashPayment_Success() {
        User user = new User();
        user.setEmail("cash@test.com");

        Booking booking = new Booking();
        booking.setBookingId(3L);
        booking.setUser(user);
        booking.setBookingCode("B-CASH");
        booking.setPaymentStatus("PENDING");

        Payment payment = new Payment();
        payment.setPaymentId(10L);
        payment.setTransactionId("txn-cash-123");
        payment.setPaymentMethod("CASH");
        payment.setStatus("PENDING");
        payment.setBooking(booking);

        when(paymentRepository.findById(10L)).thenReturn(Optional.of(payment));
        when(paymentRepository.findByTransactionIdForUpdate("txn-cash-123")).thenReturn(Optional.of(payment));

        paymentService.confirmCashPayment(10L);

        assertEquals("SUCCESS", payment.getStatus());
        assertEquals("SUCCESS", booking.getPaymentStatus());
        verify(emailService, times(1)).sendBookingConfirmationEmail("cash@test.com", "B-CASH");
        verify(paymentRepository, times(1)).save(payment);
        verify(auditLogRepository, times(1)).save(any(PaymentAuditLog.class));
    }
}
