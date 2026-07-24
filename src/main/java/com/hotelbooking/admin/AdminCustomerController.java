package com.hotelbooking.admin;

import com.hotelbooking.admin.dto.*;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.security.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/admin/customers")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    // FR-001: List customers with search/filter
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CustomerListItem>>> listCustomers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) String tier,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ctpStatus,
            @RequestParam(required = false) Boolean isVip,
            @RequestParam(required = false) LocalDateTime createdAfter,
            @RequestParam(required = false) LocalDateTime createdBefore,
            @RequestParam(required = false) LocalDateTime lastLoginAfter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        CustomerSpecification spec = CustomerSpecification.builder()
                .search(search)
                .accountType(accountType)
                .tier(tier)
                .status(status)
                .ctpStatus(ctpStatus)
                .isVip(isVip)
                .createdAfter(createdAfter)
                .createdBefore(createdBefore)
                .lastLoginAfter(lastLoginAfter)
                .build();

        Page<CustomerListItem> customers = adminCustomerService.listCustomers(spec, pageable);
        return ResponseEntity.ok(ApiResponse.success("Customers retrieved successfully", customers));
    }

    // FR-002: Customer 360° detail
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CustomerDetail360Response>> getCustomerDetail(@PathVariable Long userId) {
        CustomerDetail360Response detail = adminCustomerService.getCustomerDetail360(userId);
        return ResponseEntity.ok(ApiResponse.success("Customer details retrieved", detail));
    }

    // FR-003: VIP status
    @PutMapping("/{userId}/vip-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VipStatusResponse>> setVipStatus(
            @CurrentUser Long adminId,
            @PathVariable Long userId,
            @Valid @RequestBody VipStatusRequest request) {
        VipStatusResponse response = adminCustomerService.setVipStatus(userId, adminId, request.getIsVip());
        return ResponseEntity.ok(ApiResponse.success("VIP status updated", response));
    }

    // FR-004: Notes CRUD
    @PostMapping("/{userId}/notes")
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> addNote(
            @CurrentUser Long authorId,
            @PathVariable Long userId,
            @Valid @RequestBody CustomerNoteRequest request) {
        CustomerNoteResponse note = adminCustomerService.addNote(userId, authorId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Note added successfully", note));
    }

    @PutMapping("/{userId}/notes/{noteId}")
    public ResponseEntity<ApiResponse<CustomerNoteResponse>> updateNote(
            @CurrentUser Long authorId,
            @PathVariable Long userId,
            @PathVariable Long noteId,
            @Valid @RequestBody CustomerNoteRequest request) {
        CustomerNoteResponse note = adminCustomerService.updateNote(noteId, userId, authorId, request);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", note));
    }

    @DeleteMapping("/{userId}/notes/{noteId}")
    public ResponseEntity<ApiResponse<Void>> deleteNote(
            @PathVariable Long userId,
            @PathVariable Long noteId) {
        adminCustomerService.deleteNote(noteId, userId);
        return ResponseEntity.ok(ApiResponse.success("Note deleted", null));
    }

    @GetMapping("/{userId}/notes")
    public ResponseEntity<ApiResponse<Page<CustomerNoteResponse>>> getNotes(
            @PathVariable Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CustomerNoteResponse> notes = adminCustomerService.getNotes(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Notes retrieved successfully", notes));
    }

    // FR-006: Activity timeline
    @GetMapping("/{userId}/activity")
    public ResponseEntity<ApiResponse<Page<CustomerActivityEventResponse>>> getActivity(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CustomerActivityEventResponse> activity = adminCustomerService.getActivityTimeline(userId, type, pageable);
        return ResponseEntity.ok(ApiResponse.success("Activity timeline retrieved", activity));
    }

    // FR-007: Statistics
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CustomerStatsResponse>> getStats() {
        CustomerStatsResponse stats = adminCustomerService.getStats();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved", stats));
    }

    // FR-005: Bulk actions
    @PostMapping("/bulk-action")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BulkActionResponse>> bulkAction(
            @CurrentUser Long adminId,
            @Valid @RequestBody BulkActionRequest request) {
        BulkActionResponse response = adminCustomerService.executeBulkAction(adminId, request);
        return ResponseEntity.ok(ApiResponse.success("Bulk action executed", response));
    }
}
