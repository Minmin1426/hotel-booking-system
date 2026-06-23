package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.payment.dto.WebhookCallbackDTO;
import com.hotelbooking.user.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    private final String secret = "mock-gateway-secret-key-that-is-long-enough-for-hs256-hashing-1234567890";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "gatewaySecret", secret);
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
        requestDTO.setPaymentMethod("VNPAY");

        PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);

        assertNotNull(response);
        assertNotNull(response.getTransactionId());
        assertTrue(response.getPaymentUrl().contains("mock-gateway.com/pay"));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(auditLogRepository, times(1)).save(any());
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
    void testProcessWebhook_Success() throws Exception {
        String transactionId = "test-txn-123";
        String rawPayload = "{\"transactionId\":\"test-txn-123\",\"status\":\"SUCCESS\"}";
        
        WebhookCallbackDTO callback = new WebhookCallbackDTO();
        callback.setTransactionId(transactionId);
        callback.setStatus("SUCCESS");

        String signature = generateSignature(rawPayload, secret);

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

        paymentService.processWebhook(callback, signature, rawPayload);

        assertEquals("SUCCESS", payment.getStatus());
        assertEquals("SUCCESS", booking.getPaymentStatus());
        assertEquals("CONFIRMED", booking.getStatus());
        
        verify(paymentRepository, times(1)).save(payment);
        verify(bookingRepository, times(1)).save(booking);
        verify(emailService, times(1)).sendBookingConfirmationEmail("test@test.com", "B-12345");
        verify(auditLogRepository, times(1)).save(any());
    }

    @Test
    void testProcessWebhook_InvalidSignature() {
        WebhookCallbackDTO callback = new WebhookCallbackDTO();
        callback.setTransactionId("txn-1");

        assertThrows(SecurityException.class, () -> 
            paymentService.processWebhook(callback, "invalid-signature", "payload"));
    }

    private String generateSignature(String payload, String secret) throws Exception {
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secretKeySpec);
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
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
