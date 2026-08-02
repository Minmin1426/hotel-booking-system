package com.hotelbooking.payment.refund;

import com.hotelbooking.payment.refund.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface RefundPolicyService {

    // Policy CRUD
    List<RefundPolicyResponse> getAllPolicies();

    RefundPolicyResponse createPolicy(RefundPolicyRequest request);

    RefundPolicyResponse updatePolicy(Long policyId, RefundPolicyRequest request);

    // Policy evaluation
    RefundPreviewResponse getRefundPreview(Long bookingId, Long userId);

    RefundCalculationResult calculateRefund(
            Long bookingId, Long userId, Long adminId, BigDecimal refundOverride, String overrideReason);

    // Audit
    Page<RefundAuditLogResponse> getAuditLogs(Long bookingId, Long paymentId, Pageable pageable);

    record RefundCalculationResult(
            BigDecimal originalAmount,
            BigDecimal refundPercentage,
            BigDecimal refundAmount,
            Long policyId,
            String policyName,
            String policyDescription,
            boolean wasOverride
    ) {}
}
