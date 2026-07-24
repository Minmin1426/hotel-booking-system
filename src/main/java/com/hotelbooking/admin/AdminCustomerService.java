package com.hotelbooking.admin;

import com.hotelbooking.admin.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminCustomerService {

    // FR-001: List with search/filter
    Page<CustomerListItem> listCustomers(CustomerSpecification spec, Pageable pageable);

    // FR-002: 360° detail
    CustomerDetail360Response getCustomerDetail360(Long userId);

    // FR-003: VIP marking
    VipStatusResponse setVipStatus(Long userId, Long adminId, Boolean isVip);

    // FR-004: Notes
    CustomerNoteResponse addNote(Long userId, Long authorId, CustomerNoteRequest request);
    CustomerNoteResponse updateNote(Long noteId, Long userId, Long authorId, CustomerNoteRequest request);
    void deleteNote(Long noteId, Long userId);
    Page<CustomerNoteResponse> getNotes(Long userId, Pageable pageable);

    // FR-006: Activity timeline
    Page<CustomerActivityEventResponse> getActivityTimeline(Long userId, String eventType, Pageable pageable);

    // FR-007: Statistics
    CustomerStatsResponse getStats();

    // FR-005: Bulk actions
    BulkActionResponse executeBulkAction(Long adminId, BulkActionRequest request);

    // FR-009: Group members
    Page<GroupMemberListItem> getGroupMembers(Long groupId, Long ownerId, String search, Pageable pageable);
}
