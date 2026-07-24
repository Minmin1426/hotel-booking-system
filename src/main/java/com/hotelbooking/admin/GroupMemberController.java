package com.hotelbooking.admin;

import com.hotelbooking.admin.dto.GroupMemberListItem;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupMemberController {

    private final AdminCustomerService adminCustomerService;

    // FR-009: Group members list (GroupOwner only)
    @GetMapping("/{groupId}/members")
    @PreAuthorize("hasRole('CORPORATE_MEMBER')")
    public ResponseEntity<ApiResponse<Page<GroupMemberListItem>>> getGroupMembers(
            @CurrentUser Long userId,
            @PathVariable Long groupId,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<GroupMemberListItem> members = adminCustomerService.getGroupMembers(groupId, userId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Group members retrieved successfully", members));
    }
}
