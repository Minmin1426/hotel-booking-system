package com.hotelbooking.payment;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.payment.dto.PaymentRequestDTO;
import com.hotelbooking.payment.dto.PaymentResponseDTO;
import com.hotelbooking.payment.refund.RefundAuditLogRepository;
import com.hotelbooking.payment.refund.RefundPolicy;
import com.hotelbooking.payment.refund.RefundPolicyRepository;
import com.hotelbooking.user.User;
import com.hotelbooking.voucher.VoucherRepository;
import com.hotelbooking.voucher.VoucherStoreService;
import com.hotelbooking.wallet.Wallet;
import com.hotelbooking.wallet.WalletRepository;
import com.hotelbooking.wallet.WalletService;
import com.hotelbooking.wallet.WalletType;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceSpecTest {

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
    private RefundPolicyRepository refundPolicyRepository;

    @Mock
    private RefundAuditLogRepository refundAuditLogRepository;

    @Mock
    private WalletService walletService;

    @Mock
    private WalletRepository walletRepository;

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
    void testCreatePaymentRequest_InvalidDepositRatio() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentRequestDTO requestDTO = new PaymentRequestDTO();
        requestDTO.setBookingId(1L);
        requestDTO.setPaymentMethod("STRIPE");
        requestDTO.setIsDeposit(true);
        requestDTO.setDepositRatio(BigDecimal.valueOf(0.20)); // Below 0.30

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> paymentService.createPaymentRequest(requestDTO));
        assertTrue(exception.getMessage().contains("Deposit ratio must be between"));
    }

    @Test
    void testCreatePaymentRequest_ReusesPendingPayment() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));

        Payment pendingPayment = new Payment();
        pendingPayment.setPaymentId(99L);
        pendingPayment.setStatus("PENDING");
        pendingPayment.setBooking(booking);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBookingBookingId(1L)).thenReturn(Collections.singletonList(pendingPayment));

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
            assertEquals(Long.valueOf(99), pendingPayment.getPaymentId());
            assertEquals("STRIPE", pendingPayment.getPaymentMethod());
            verify(paymentRepository, times(1)).save(pendingPayment);
        }
    }

    @Test
    void testProcessRefund_DynamicRefundCalculation() {
        Booking booking = new Booking();
        User user = new User();
        user.setEmail("test@test.com");
        booking.setUser(user);
        booking.setBookingId(1L);
        booking.setStatus("CANCELLED");
        booking.setCheckInDate(LocalDateTime.now().plusDays(5)); // Check-in is 5 days from now
        booking.setTotalAmount(BigDecimal.valueOf(1000));
        booking.setPaymentStatus("SUCCESS");

        Payment payment = new Payment();
        payment.setPaymentId(100L);
        payment.setBooking(booking);
        payment.setStatus("REFUND_PENDING");
        payment.setAmount(BigDecimal.valueOf(1000));
        payment.setUpdatedAt(LocalDateTime.now()); // Dynamic cancellation date reference point

        RefundPolicy policy = new RefundPolicy();
        policy.setDaysBeforeCheckin(3);
        policy.setRefundPercentage(BigDecimal.valueOf(80)); // 80% refund within >= 3 days

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_BookingId(1L)).thenReturn(Optional.of(payment));
        when(refundPolicyRepository.findMatchingPoliciesOrdered(anyInt())).thenReturn(Collections.singletonList(policy));

        paymentService.processRefund(1L);

        // Verify 80% refund logic
        verify(refundAuditLogRepository).save(argThat(log -> 
            log.getRefundAmount().compareTo(BigDecimal.valueOf(800.00)) == 0 &&
            log.getRefundPercentage().compareTo(BigDecimal.valueOf(80)) == 0
        ));
    }

    @Test
    void testRefundUnusedMealTickets_WalletDeposited() {
        User user = new User();
        user.setUserId(45L);
        user.setEmail("guest@example.com");

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setUser(user);

        Payment payment = new Payment();
        payment.setPaymentId(100L);
        payment.setBooking(booking);
        payment.setStatus("SUCCESS");
        payment.setAmount(BigDecimal.valueOf(500.00));
        payment.setMealRefundAmount(BigDecimal.ZERO);

        Wallet wallet = new Wallet();
        wallet.setWalletId(1L);
        wallet.setOwnerUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.findByBooking_BookingId(1L)).thenReturn(Optional.of(payment));
        when(walletRepository.findByOwnerUserUserIdAndWalletType(45L, WalletType.PERSONAL)).thenReturn(Optional.of(wallet));

        paymentService.refundUnusedMealTickets(1L, BigDecimal.valueOf(120.00));

        // Verify WalletService.refundToWallet is called with walletId 1, bookingId 1, amount 120.00
        verify(walletService).refundToWallet(eq(1L), eq(1L), eq(BigDecimal.valueOf(120.00)));
    }
}
