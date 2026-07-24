package com.hotelbooking.payment.refund;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.payment.Payment;
import com.hotelbooking.payment.PaymentRepository;
import com.hotelbooking.payment.refund.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundPolicyServiceImpl implements RefundPolicyService {

    private final RefundPolicyRepository refundPolicyRepository;
    private final RefundAuditLogRepository refundAuditLogRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    // ── Policy CRUD ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RefundPolicyResponse> getAllPolicies() {
        return refundPolicyRepository.findAllByOrderByPriorityAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RefundPolicyResponse createPolicy(RefundPolicyRequest request) {
        RefundPolicy policy = RefundPolicy.builder()
                .name(request.getName())
                .daysBeforeCheckin(request.getDaysBeforeCheckin())
                .refundPercentage(request.getRefundPercentage())
                .description(request.getDescription())
                .priority(request.getPriority())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        RefundPolicy saved = refundPolicyRepository.save(policy);
        log.info("RefundPolicy: Created policy {} with {}% refund for {} days before check-in",
                saved.getPolicyId(), saved.getRefundPercentage(), saved.getDaysBeforeCheckin());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public RefundPolicyResponse updatePolicy(Long policyId, RefundPolicyRequest request) {
        RefundPolicy policy = refundPolicyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("RefundPolicy", policyId));

        policy.setName(request.getName());
        policy.setDaysBeforeCheckin(request.getDaysBeforeCheckin());
        policy.setRefundPercentage(request.getRefundPercentage());
        policy.setDescription(request.getDescription());
        policy.setPriority(request.getPriority());
        if (request.getIsActive() != null) {
            policy.setIsActive(request.getIsActive());
        }

        RefundPolicy saved = refundPolicyRepository.save(policy);
        log.info("RefundPolicy: Updated policy {}", policyId);
        return toResponse(saved);
    }

    // ── Refund Preview ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public RefundPreviewResponse getRefundPreview(Long bookingId, Long userId) {
        Booking booking = getBookingAndValidateAccess(bookingId, userId);

        // Get the successful payment amount
        BigDecimal originalAmount = getSuccessfulPaymentAmount(booking);

        int daysRemaining = calculateDaysRemaining(booking);

        // Find matching policy
        RefundPolicy policy = findMatchingPolicy(daysRemaining);

        BigDecimal refundPercentage = policy != null
                ? policy.getRefundPercentage()
                : BigDecimal.ZERO;

        BigDecimal refundAmount = originalAmount
                .multiply(refundPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        return RefundPreviewResponse.builder()
                .bookingId(bookingId)
                .originalAmount(originalAmount)
                .refundPercentage(refundPercentage)
                .refundAmount(refundAmount)
                .policyName(policy != null ? policy.getName() : null)
                .policyDescription(policy != null ? policy.getDescription() : null)
                .daysRemaining(daysRemaining)
                .build();
    }

    // ── Calculate Refund + Audit ───────────────────────────────────────────────

    @Override
    @Transactional
    public RefundCalculationResult calculateRefund(
            Long bookingId, Long userId, Long adminId,
            BigDecimal refundOverride, String overrideReason) {

        Booking booking = getBookingAndValidateAccess(bookingId, userId);
        validateBookingCanBeRefunded(booking);

        BigDecimal originalAmount = getSuccessfulPaymentAmount(booking);
        int daysRemaining = calculateDaysRemaining(booking);

        BigDecimal refundPercentage;
        Long policyId = null;
        String policyName = null;
        String policyDescription = null;
        boolean wasOverride = false;

        if (refundOverride != null) {
            // Admin override takes precedence
            if (refundOverride.compareTo(BigDecimal.ZERO) < 0 || refundOverride.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException("INVALID_REFUND_PERCENTAGE: Override must be between 0 and 100");
            }
            refundPercentage = refundOverride;
            wasOverride = true;
            log.info("RefundPolicy: Override applied for booking {}: {}%", bookingId, refundOverride);
        } else {
            RefundPolicy policy = findMatchingPolicy(daysRemaining);
            if (policy != null) {
                refundPercentage = policy.getRefundPercentage();
                policyId = policy.getPolicyId();
                policyName = policy.getName();
                policyDescription = policy.getDescription();
            } else {
                refundPercentage = BigDecimal.ZERO;
            }
        }

        BigDecimal refundAmount = originalAmount
                .multiply(refundPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Record audit log
        Payment payment = getSuccessfulPayment(booking);
        recordAudit(
                booking.getBookingId(),
                payment != null ? payment.getPaymentId() : null,
                originalAmount,
                refundPercentage,
                refundAmount,
                adminId,
                wasOverride ? overrideReason : null,
                payment != null ? payment.getStatus() : null,
                "REFUND_PENDING",
                policyId
        );

        return new RefundCalculationResult(
                originalAmount, refundPercentage, refundAmount,
                policyId, policyName, policyDescription, wasOverride);
    }

    // ── Audit Logs ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<RefundAuditLogResponse> getAuditLogs(Long bookingId, Long paymentId, Pageable pageable) {
        Page<RefundAuditLog> logs;
        if (bookingId != null) {
            logs = refundAuditLogRepository.findByBookingIdOrderByCreatedAtDesc(bookingId, pageable);
        } else if (paymentId != null) {
            logs = refundAuditLogRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId, pageable);
        } else {
            logs = refundAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return logs.map(this::toAuditResponse);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Booking getBookingAndValidateAccess(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        // Admins can access any booking
        // For non-admin access, validate ownership
        // (Caller should verify role — here we just check if ownership matches if userId provided)
        if (userId != null && !booking.getUser().getUserId().equals(userId)) {
            throw new BusinessException("Access denied: You do not own this booking");
        }
        return booking;
    }

    private void validateBookingCanBeRefunded(Booking booking) {
        if (!"CONFIRMED".equalsIgnoreCase(booking.getStatus())
                && !"PENDING".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("BOOKING_NOT_CONFIRMED");
        }
        if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
            throw new BusinessException("BOOKING_ALREADY_CANCELLED");
        }
    }

    private BigDecimal getSuccessfulPaymentAmount(Booking booking) {
        Payment payment = getSuccessfulPayment(booking);
        if (payment == null) {
            throw new BusinessException("NO_PAYMENT_FOUND");
        }
        if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
            throw new BusinessException("PAYMENT_ALREADY_REFUNDED");
        }
        return payment.getAmount();
    }

    private Payment getSuccessfulPayment(Booking booking) {
        List<Payment> payments = paymentRepository.findByBookingBookingId(booking.getBookingId());
        return payments.stream()
                .filter(p -> "SUCCESS".equalsIgnoreCase(p.getStatus())
                        || "COMPLETED".equalsIgnoreCase(p.getStatus()))
                .findFirst()
                .orElse(null);
    }

    int calculateDaysRemaining(Booking booking) {
        if (booking.getCheckInDate() == null) {
            return 0;
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), booking.getCheckInDate());
        return (int) Math.max(0, days);
    }

    RefundPolicy findMatchingPolicy(int daysRemaining) {
        List<RefundPolicy> matches = refundPolicyRepository.findMatchingPoliciesOrdered(daysRemaining);
        return matches.isEmpty() ? null : matches.get(0);
    }

    private void recordAudit(
            Long bookingId, Long paymentId,
            BigDecimal originalAmount, BigDecimal refundPercentage, BigDecimal refundAmount,
            Long overrideBy, String overrideReason,
            String previousStatus, String newStatus,
            Long policyId) {

        RefundAuditLog auditLog = RefundAuditLog.builder()
                .bookingId(bookingId)
                .paymentId(paymentId)
                .originalAmount(originalAmount)
                .refundPercentage(refundPercentage)
                .refundAmount(refundAmount)
                .overrideBy(overrideBy)
                .overrideReason(overrideReason)
                .previousPaymentStatus(previousStatus)
                .newPaymentStatus(newStatus)
                .policyId(policyId)
                .build();
        refundAuditLogRepository.save(auditLog);
        log.debug("RefundAuditLog: Recorded for bookingId={}, amount={}", bookingId, refundAmount);
    }

    private RefundPolicyResponse toResponse(RefundPolicy policy) {
        return RefundPolicyResponse.builder()
                .policyId(policy.getPolicyId())
                .name(policy.getName())
                .daysBeforeCheckin(policy.getDaysBeforeCheckin())
                .refundPercentage(policy.getRefundPercentage())
                .description(policy.getDescription())
                .priority(policy.getPriority())
                .isActive(policy.getIsActive())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    private RefundAuditLogResponse toAuditResponse(RefundAuditLog log) {
        return RefundAuditLogResponse.builder()
                .auditId(log.getAuditId())
                .bookingId(log.getBookingId())
                .paymentId(log.getPaymentId())
                .originalAmount(log.getOriginalAmount())
                .refundPercentage(log.getRefundPercentage())
                .refundAmount(log.getRefundAmount())
                .overrideBy(log.getOverrideBy())
                .overrideReason(log.getOverrideReason())
                .previousPaymentStatus(log.getPreviousPaymentStatus())
                .newPaymentStatus(log.getNewPaymentStatus())
                .policyId(log.getPolicyId())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
