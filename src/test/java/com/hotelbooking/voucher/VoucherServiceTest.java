package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.voucher.dto.VoucherResponse;

import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    void testApplyVoucher_Success_Percentage() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING_PAYMENT");
        booking.setTotalAmount(BigDecimal.valueOf(1000));

        Voucher voucher = new Voucher();
        voucher.setCode("DISCOUNT20");
        voucher.setDiscountType("PERCENTAGE");
        voucher.setDiscountValue(BigDecimal.valueOf(20)); // 20%
        voucher.setCurrentUsage(0);
        voucher.setMaxUsage(100);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("DISCOUNT20")).thenReturn(Optional.of(voucher));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = voucherService.applyVoucher(1L, "DISCOUNT20");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200.0), result.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(800.0), result.getFinalPrice());
        assertEquals(1, voucher.getCurrentUsage());
    }

    @Test
    void testApplyVoucher_Expired() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING_PAYMENT");
        booking.setTotalAmount(BigDecimal.valueOf(1000));

        Voucher voucher = new Voucher();
        voucher.setCode("EXPIRED");
        voucher.setEndDate(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("EXPIRED")).thenReturn(Optional.of(voucher));

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> voucherService.applyVoucher(1L, "EXPIRED"));
        assertEquals("Voucher has expired.", exception.getMessage());
    }

    @Test
    void testApplyVoucher_MinBookingValueNotMet() {
        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING_PAYMENT");
        booking.setTotalAmount(BigDecimal.valueOf(500));

        Voucher voucher = new Voucher();
        voucher.setCode("MIN1000");
        voucher.setMinBookingValue(BigDecimal.valueOf(1000));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("MIN1000")).thenReturn(Optional.of(voucher));

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> voucherService.applyVoucher(1L, "MIN1000"));
        assertEquals("Booking total does not meet the minimum value requirement for this voucher.", exception.getMessage());
    }

    @Test
    void testGetAllActiveVouchers_Success() {
        Voucher active1 = Voucher.builder()
                .voucherId(1L)
                .code("ACTIVE1")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(10))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .maxUsage(10)
                .currentUsage(2)
                .build();

        Voucher expired = Voucher.builder()
                .voucherId(2L)
                .code("EXPIRED")
                .discountType("FIXED_AMOUNT")
                .discountValue(BigDecimal.valueOf(50))
                .endDate(LocalDateTime.now().minusDays(1))
                .build();

        Voucher exhausted = Voucher.builder()
                .voucherId(3L)
                .code("EXHAUSTED")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(15))
                .maxUsage(5)
                .currentUsage(5)
                .build();

        when(voucherRepository.findAll()).thenReturn(List.of(active1, expired, exhausted));

        List<VoucherResponse> result = voucherService.getAllActiveVouchers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTIVE1", result.get(0).getCode());
    }
}
