package com.hotelbooking.payment;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.user.User;
import com.hotelbooking.voucher.VoucherRepository;
import com.hotelbooking.voucher.VoucherStoreService;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.mockito.MockedStatic;
import java.time.LocalDateTime;

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

    @Mock
    private VnpayService vnpayService;

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private VoucherStoreService voucherStoreService;

    @Mock
    private com.hotelbooking.payment.refund.RefundPolicyRepository refundPolicyRepository;

    @Mock
    private com.hotelbooking.payment.refund.RefundAuditLogRepository refundAuditLogRepository;

    @Mock
    private com.hotelbooking.wallet.WalletRepository walletRepository;

    @Mock
    private com.hotelbooking.wallet.WalletService walletService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final String stripeKey = "dummy_api_key";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "stripeApiKey", stripeKey);
        ReflectionTestUtils.setField(paymentService, "stripeWebhookSecret", "dummy_webhook_token");
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

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getId()).thenReturn("pi_test_123");
            when(mockIntent.getClientSecret()).thenReturn("seti_test_token_123");
            
            mockedPaymentIntent.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                    .thenReturn(mockIntent);

            PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);

            assertNotNull(response);
            assertEquals("pi_test_123", response.getTransactionId());
            assertEquals("seti_test_token_123", response.getClientSecret());
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
        String transactionId = "pi_test_123";

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

        try (MockedStatic<PaymentIntent> mockedPaymentIntent = mockStatic(PaymentIntent.class)) {
            PaymentIntent mockIntent = mock(PaymentIntent.class);
            when(mockIntent.getStatus()).thenReturn("succeeded");
            
            mockedPaymentIntent.when(() -> PaymentIntent.retrieve(transactionId))
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
        booking.setCheckInDate(LocalDateTime.now().plusDays(10));
        
        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setStatus("SUCCESS");
        payment.setBooking(booking);

        com.hotelbooking.payment.refund.RefundPolicy policy = new com.hotelbooking.payment.refund.RefundPolicy();
        policy.setDaysBeforeCheckin(3);
        policy.setRefundPercentage(BigDecimal.valueOf(50)); // 50% refund

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_BookingId(1L)).thenReturn(Optional.of(payment));
        when(refundPolicyRepository.findMatchingPoliciesOrdered(anyInt()))
                .thenReturn(java.util.Collections.singletonList(policy));

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
        payment.setRefundAmount(BigDecimal.valueOf(500));
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

    @Test
    void testCreatePaymentRequest_Deposit_Vnpay() {
        Booking booking = new Booking();
        booking.setBookingId(4L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));
        booking.setBookingCode("B-DEPOSIT");

        when(bookingRepository.findById(4L)).thenReturn(Optional.of(booking));
        when(vnpayService.createPaymentUrl(any(), any(), any())).thenReturn("http://mock-vnpay-url.com");

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setBookingId(4L);
        requestDTO.setPaymentMethod("VNPAY");
        requestDTO.setIsDeposit(true);
        requestDTO.setDepositRatio(new BigDecimal("0.50"));
        requestDTO.setCompanyName("Test Company");
        requestDTO.setTaxId("12345");

        PaymentResponseDTO response = paymentService.createPaymentRequest(requestDTO);

        assertNotNull(response);
        assertEquals("http://mock-vnpay-url.com", response.getPaymentUrl());
        assertEquals(true, response.getIsDeposit());
        assertEquals(new BigDecimal("0.50"), response.getDepositRatio());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testCheckExpiredPaymentHolds() {
        Booking booking = new Booking();
        booking.setBookingId(5L);
        booking.setPaymentStatus("PENDING");
        booking.setStatus("PENDING");

        Payment payment = new Payment();
        payment.setTransactionId("txn-expired");
        payment.setStatus("PENDING");
        payment.setCountdownEndTime(LocalDateTime.now().minusMinutes(1));
        payment.setBooking(booking);

        when(paymentRepository.findExpiredPayments(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(java.util.Collections.singletonList(payment));

        paymentService.checkExpiredPaymentHolds();

        assertEquals("FAILED", payment.getStatus());
        assertEquals("FAILED", booking.getStatus());
        assertEquals("FAILED", booking.getPaymentStatus());
        verify(paymentRepository, times(1)).save(payment);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void testRefundUnusedMealTickets_Success() {
        User user = new User();
        user.setUserId(45L);

        Booking booking = new Booking();
        booking.setBookingId(6L);
        booking.setUser(user);

        Payment payment = new Payment();
        payment.setAmount(BigDecimal.valueOf(500));
        payment.setStatus("SUCCESS");
        payment.setBooking(booking);

        com.hotelbooking.wallet.Wallet wallet = new com.hotelbooking.wallet.Wallet();
        wallet.setWalletId(1L);
        wallet.setOwnerUser(user);

        when(bookingRepository.findById(6L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_BookingId(6L)).thenReturn(Optional.of(payment));
        when(walletRepository.findByOwnerUserUserIdAndWalletType(45L, com.hotelbooking.wallet.WalletType.PERSONAL))
                .thenReturn(Optional.of(wallet));

        paymentService.refundUnusedMealTickets(6L, BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), payment.getMealRefundAmount());
        assertEquals(BigDecimal.valueOf(50), payment.getRefundAmount());
        verify(paymentRepository, times(1)).save(payment);
        verify(walletService, times(1)).refundToWallet(eq(1L), eq(6L), eq(BigDecimal.valueOf(50)));
    }

    @Test
    void testGenerateInvoicePdf_Success() {
        Booking booking = new Booking();
        booking.setBookingCode("B-INVOICE");

        Payment payment = new Payment();
        payment.setPaymentId(100L);
        payment.setTransactionId("txn-invoice");
        payment.setAmount(BigDecimal.valueOf(300));
        payment.setBooking(booking);
        payment.setInvoiceCompanyName("Test Corp");
        payment.setInvoiceTaxId("TAX-999");

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        byte[] pdfBytes = paymentService.generateInvoicePdf(100L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void testCalculateMonthlyPayout() {
        Payment payment1 = new Payment();
        payment1.setAmount(BigDecimal.valueOf(100));
        Payment payment2 = new Payment();
        payment2.setAmount(BigDecimal.valueOf(200));

        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        when(paymentRepository.findSuccessfulPaymentsByHotelAndDate(1L, start, end))
                .thenReturn(java.util.List.of(payment1, payment2));
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payout payout = paymentService.calculateMonthlyPayout(1L, start, end);

        assertNotNull(payout);
        assertEquals(new BigDecimal("300"), payout.getTotalRevenue());
        assertEquals(new BigDecimal("270.00"), payout.getPayoutAmount()); // 300 - 10% commission
    }

    @Test
    void testApprovePayout() {
        Payout payout = new Payout();
        payout.setPayoutId(10L);
        payout.setStatus("PENDING");
        payout.setHotelId(1L);
        payout.setPayoutAmount(BigDecimal.valueOf(270));

        when(payoutRepository.findById(10L)).thenReturn(Optional.of(payout));

        paymentService.approvePayout(10L);

        assertEquals("PAID", payout.getStatus());
        verify(payoutRepository, times(1)).save(payout);
    }
}
