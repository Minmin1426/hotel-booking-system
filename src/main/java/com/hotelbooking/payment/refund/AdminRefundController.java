package com.hotelbooking.payment.refund;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.payment.refund.dto.RefundAuditLogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/refund-audit-logs")
@PreAuthorize("hasAnyRole('ADMIN', 'FINANCE')")
@RequiredArgsConstructor
@Slf4j
public class AdminRefundController {

    private final RefundPolicyService refundPolicyService;

    // GET /api/v1/admin/refund-audit-logs?bookingId=&paymentId=&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<Page<RefundAuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long paymentId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<RefundAuditLogResponse> logs = refundPolicyService.getAuditLogs(bookingId, paymentId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Refund audit logs retrieved", logs));
    }
}
