package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.batch.dto.BlockBookingResponseDto;
import com.hotelbooking.booking.batch.dto.RejectRequestDto;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/block-bookings")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminBatchBookingController {

    private final BatchBookingService batchBookingService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlockBookingResponseDto>>> getAllRequests(
            @RequestParam(required = false) String status,
            Pageable pageable) {
        Page<BlockBookingResponseDto> requests = batchBookingService.getAllRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success("All block booking requests retrieved", requests));
    }

    @GetMapping("/{blockBookingId}")
    public ResponseEntity<ApiResponse<BlockBookingResponseDto>> getRequestDetails(
            @PathVariable Long blockBookingId) {
        BlockBookingResponseDto request = batchBookingService.getAllRequestDetails(blockBookingId);
        return ResponseEntity.ok(ApiResponse.success("Block booking request details", request));
    }

    @PostMapping("/{blockBookingId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRequest(
            @CurrentUser Long approverId,
            @PathVariable Long blockBookingId) {
        batchBookingService.approveRequest(approverId, blockBookingId);
        return ResponseEntity.ok(ApiResponse.success("Request approved and processing started", null));
    }

    @PostMapping("/{blockBookingId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @CurrentUser Long approverId,
            @PathVariable Long blockBookingId,
            @Valid @RequestBody RejectRequestDto rejectDto) {
        batchBookingService.rejectRequest(approverId, blockBookingId, rejectDto.getReason());
        return ResponseEntity.ok(ApiResponse.success("Request rejected", null));
    }

    @PostMapping("/{blockBookingId}/process")
    public ResponseEntity<ApiResponse<Void>> processRequest(
            @PathVariable Long blockBookingId) {
        batchBookingService.processApprovedRequest(blockBookingId);
        return ResponseEntity.ok(ApiResponse.success("Processing started", null));
    }
}
