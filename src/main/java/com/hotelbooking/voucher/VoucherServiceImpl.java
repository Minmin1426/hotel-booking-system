package com.hotelbooking.voucher;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.voucher.dto.VoucherResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public Booking applyVoucher(Long bookingId, String voucherCode) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId.toString()));

        if (!"DRAFT".equals(booking.getStatus()) && !"PENDING_PAYMENT".equals(booking.getStatus())) {
            throw new BusinessException("Cannot apply voucher to booking in status: " + booking.getStatus());
        }

        if (booking.getVoucher() != null) {
            throw new BusinessException("A voucher has already been applied to this booking.");
        }

        Voucher voucher = voucherRepository.findByCode(voucherCode)
                .orElseThrow(() -> new BusinessException("Voucher does not exist."));

        LocalDateTime now = LocalDateTime.now();

        if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
            throw new BusinessException("Voucher is not yet active.");
        }

        if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
            throw new BusinessException("Voucher has expired.");
        }

        if (voucher.getMaxUsage() != null && voucher.getMaxUsage() > 0 && 
            voucher.getCurrentUsage() != null && voucher.getCurrentUsage() >= voucher.getMaxUsage()) {
            throw new BusinessException("Voucher has reached its usage limit.");
        }

        if (voucher.getMinBookingValue() != null && 
            booking.getTotalAmount().compareTo(voucher.getMinBookingValue()) < 0) {
            throw new BusinessException("Booking total does not meet the minimum value requirement for this voucher.");
        }

        BigDecimal discountAmount;
        if ("PERCENTAGE".equalsIgnoreCase(voucher.getDiscountType())) {
            BigDecimal percentage = voucher.getDiscountValue().divide(BigDecimal.valueOf(100));
            discountAmount = booking.getTotalAmount().multiply(percentage);
        } else {
            discountAmount = voucher.getDiscountValue();
        }

        // Limit discount to total booking amount
        if (discountAmount.compareTo(booking.getTotalAmount()) > 0) {
            discountAmount = booking.getTotalAmount();
        }

        BigDecimal finalPrice = booking.getTotalAmount().subtract(discountAmount);

        booking.setVoucher(voucher);
        booking.setDiscountAmount(discountAmount);
        booking.setFinalPrice(finalPrice);

        // Update voucher usage
        if (voucher.getCurrentUsage() == null) {
            voucher.setCurrentUsage(1);
        } else {
            voucher.setCurrentUsage(voucher.getCurrentUsage() + 1);
        }

        voucherRepository.save(voucher);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllActiveVouchers() {
        log.info("Retrieving all active vouchers for guests");
        LocalDateTime now = LocalDateTime.now();
        return voucherRepository.findAll().stream()
                .filter(v -> (v.getStartDate() == null || now.isAfter(v.getStartDate())))
                .filter(v -> (v.getEndDate() == null || now.isBefore(v.getEndDate())))
                .filter(v -> (v.getMaxUsage() == null || v.getMaxUsage() == 0 ||
                             v.getCurrentUsage() == null || v.getCurrentUsage() < v.getMaxUsage()))
                .map(this::mapToResponse)
                .toList();
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .voucherId(voucher.getVoucherId())
                .code(voucher.getCode())
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .minBookingValue(voucher.getMinBookingValue())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .maxUsage(voucher.getMaxUsage())
                .currentUsage(voucher.getCurrentUsage())
                .build();
    }
}
