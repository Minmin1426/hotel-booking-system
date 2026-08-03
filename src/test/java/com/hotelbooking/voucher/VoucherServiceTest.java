package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.voucher.dto.VoucherResponse;

import com.hotelbooking.user.User;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserVoucherRepository userVoucherRepository;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    @Test
    void testApplyVoucher_Success_Percentage() {
        User user = new User();
        user.setUserId(1L);

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));
        booking.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setCode("DISCOUNT20");
        voucher.setDiscountType("PERCENTAGE");
        voucher.setDiscountValue(BigDecimal.valueOf(20)); // 20%
        voucher.setCurrentUsage(0);
        voucher.setMaxUsage(100);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("DISCOUNT20")).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserUserIdAndVoucherCode(any(), eq("DISCOUNT20")))
                .thenReturn(Optional.of(new UserVoucher()));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = voucherService.applyVoucher(1L, "DISCOUNT20");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(200.0), result.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(800.0), result.getFinalPrice());
    }

    @Test
    void testApplyVoucher_Expired() {
        User user = new User();
        user.setUserId(1L);

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));
        booking.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setCode("EXPIRED");
        voucher.setEndDate(LocalDateTime.now().minusDays(1));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("EXPIRED")).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserUserIdAndVoucherCode(any(), eq("EXPIRED")))
                .thenReturn(Optional.of(new UserVoucher()));

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> voucherService.applyVoucher(1L, "EXPIRED"));
        assertEquals("Voucher has expired.", exception.getMessage());
    }

    @Test
    void testApplyVoucher_MinBookingValueNotMet() {
        User user = new User();
        user.setUserId(1L);

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(500));
        booking.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setCode("MIN1000");
        voucher.setMinBookingValue(BigDecimal.valueOf(1000));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("MIN1000")).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserUserIdAndVoucherCode(any(), eq("MIN1000")))
                .thenReturn(Optional.of(new UserVoucher()));

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

    @Test
    void testGetAllActiveVouchersForUser() {
        Voucher active1 = Voucher.builder()
                .voucherId(1L)
                .code("ACTIVE1")
                .discountType("PERCENTAGE")
                .discountValue(BigDecimal.valueOf(10))
                .currentUsage(0)
                .maxUsage(10)
                .build();

        Voucher active2 = Voucher.builder()
                .voucherId(2L)
                .code("ACTIVE2")
                .discountType("FIXED_AMOUNT")
                .discountValue(BigDecimal.valueOf(50))
                .currentUsage(0)
                .maxUsage(10)
                .build();

        User user = new User();
        user.setUserId(1L);

        UserVoucher uv = UserVoucher.builder()
                .user(user)
                .voucher(active1)
                .isUsed(true)
                .build();

        org.springframework.data.domain.Page<UserVoucher> userVoucherPage = new org.springframework.data.domain.PageImpl<>(List.of(uv));

        when(voucherRepository.findAll()).thenReturn(List.of(active1, active2));
        when(userVoucherRepository.findByUserUserIdAndVoucherVoucherId(eq(1L), eq(1L)))
                .thenReturn(Optional.of(uv));
        when(userVoucherRepository.findByUserUserIdAndVoucherVoucherId(eq(1L), eq(2L)))
                .thenReturn(Optional.empty());

        List<VoucherResponse> result = voucherService.getAllActiveVouchersForUser(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        
        VoucherResponse resp1 = result.stream().filter(r -> r.getCode().equals("ACTIVE1")).findFirst().orElseThrow();
        assertTrue(resp1.getIsClaimed());
        assertTrue(resp1.getIsUsed());

        VoucherResponse resp2 = result.stream().filter(r -> r.getCode().equals("ACTIVE2")).findFirst().orElseThrow();
        assertFalse(resp2.getIsClaimed());
        assertFalse(resp2.getIsUsed());
    }

    @Test
    void testApplyVoucher_Combo() {
        User user = new User();
        user.setUserId(1L);

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setTotalAmount(BigDecimal.valueOf(1000));
        booking.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setCode("COMBOMEAL");
        voucher.setDiscountType("FIXED_AMOUNT");
        voucher.setDiscountValue(BigDecimal.valueOf(50));
        voucher.setVoucherType("ROOM_MEAL_COMBO");
        voucher.setComboMealBenefit("FREE_BREAKFAST");
        voucher.setCurrentUsage(0);
        voucher.setMaxUsage(10);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("COMBOMEAL")).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserUserIdAndVoucherCode(any(), eq("COMBOMEAL")))
                .thenReturn(Optional.of(new UserVoucher()));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        Booking result = voucherService.applyVoucher(1L, "COMBOMEAL");

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(50), result.getDiscountAmount());
        assertEquals(BigDecimal.valueOf(950), result.getFinalPrice());
        assertEquals("ROOM_MEAL_COMBO", result.getVoucher().getVoucherType());
        assertEquals("FREE_BREAKFAST", result.getVoucher().getComboMealBenefit());
    }

    @Test
    void testApplyVoucher_NotClaimed() {
        User user = new User();
        user.setUserId(1L);

        Booking booking = new Booking();
        booking.setBookingId(1L);
        booking.setStatus("PENDING");
        booking.setUser(user);

        Voucher voucher = new Voucher();
        voucher.setCode("DISCOUNT20");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(voucherRepository.findByCode("DISCOUNT20")).thenReturn(Optional.of(voucher));
        when(userVoucherRepository.findByUserUserIdAndVoucherCode(any(), eq("DISCOUNT20")))
                .thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, 
            () -> voucherService.applyVoucher(1L, "DISCOUNT20"));
        assertTrue(exception.getMessage().contains("VOUCHER_NOT_CLAIMED"));
    }
}
