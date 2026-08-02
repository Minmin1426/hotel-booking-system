package com.hotelbooking.payment.refund;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.payment.refund.dto.RefundPolicyRequest;
import com.hotelbooking.payment.refund.dto.RefundPolicyResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/refund-policies")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class RefundPolicyController {

    private final RefundPolicyService refundPolicyService;

    // GET /api/v1/admin/refund-policies
    @GetMapping
    public ResponseEntity<ApiResponse<List<RefundPolicyResponse>>> getAllPolicies() {
        List<RefundPolicyResponse> policies = refundPolicyService.getAllPolicies();
        return ResponseEntity.ok(ApiResponse.success("Refund policies retrieved", policies));
    }

    // POST /api/v1/admin/refund-policies
    @PostMapping
    public ResponseEntity<ApiResponse<RefundPolicyResponse>> createPolicy(
            @Valid @RequestBody RefundPolicyRequest request) {
        RefundPolicyResponse response = refundPolicyService.createPolicy(request);
        return ResponseEntity.ok(ApiResponse.success("Refund policy created", response));
    }

    // PUT /api/v1/admin/refund-policies/{policyId}
    @PutMapping("/{policyId}")
    public ResponseEntity<ApiResponse<RefundPolicyResponse>> updatePolicy(
            @PathVariable Long policyId,
            @Valid @RequestBody RefundPolicyRequest request) {
        RefundPolicyResponse response = refundPolicyService.updatePolicy(policyId, request);
        return ResponseEntity.ok(ApiResponse.success("Refund policy updated", response));
    }
}
