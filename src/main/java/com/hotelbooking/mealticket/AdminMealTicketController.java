package com.hotelbooking.mealticket;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.mealticket.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminMealTicketController {

    private final MealTicketService mealTicketService;
    private final MealTicketTypeRepository typeRepository;
    private final JwtService jwtService;

    // GET /api/v1/admin/meal-ticket-types
    @GetMapping("/admin/meal-ticket-types")
    public ResponseEntity<ApiResponse<List<MealTicketTypeResponse>>> getTicketTypes() {
        List<MealTicketTypeResponse> types = typeRepository.findAll().stream()
                .map(t -> MealTicketTypeResponse.builder()
                        .typeId(t.getTypeId())
                        .code(t.getCode())
                        .name(t.getName())
                        .description(t.getDescription())
                        .defaultValidDays(t.getDefaultValidDays())
                        .defaultPrice(t.getDefaultPrice())
                        .isActive(t.getIsActive())
                        .build())
                .toList();
        return ResponseEntity.ok(ApiResponse.success("Ticket types retrieved", types));
    }

    // POST /api/v1/admin/meal-ticket-types
    @PostMapping("/admin/meal-ticket-types")
    public ResponseEntity<ApiResponse<MealTicketTypeResponse>> createTicketType(
            @Valid @RequestBody MealTicketTypeResponse request) {
        MealTicketType type = MealTicketType.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .defaultValidDays(request.getDefaultValidDays() != null ? request.getDefaultValidDays() : 30)
                .defaultPrice(request.getDefaultPrice() != null ? request.getDefaultPrice() : java.math.BigDecimal.ZERO)
                .isActive(true)
                .build();
        type = typeRepository.save(type);
        return ResponseEntity.ok(ApiResponse.success("Ticket type created",
                toTypeResponse(type)));
    }

    // PUT /api/v1/admin/meal-ticket-types/{typeId}
    @PutMapping("/admin/meal-ticket-types/{typeId}")
    public ResponseEntity<ApiResponse<MealTicketTypeResponse>> updateTicketType(
            @PathVariable Long typeId,
            @Valid @RequestBody MealTicketTypeResponse request) {
        MealTicketType type = typeRepository.findById(typeId)
                .orElseThrow(() -> new com.hotelbooking.common.exception.ResourceNotFoundException(
                        "MealTicketType", "id", typeId.toString()));
        if (request.getName() != null) type.setName(request.getName());
        if (request.getDescription() != null) type.setDescription(request.getDescription());
        if (request.getDefaultValidDays() != null) type.setDefaultValidDays(request.getDefaultValidDays());
        if (request.getDefaultPrice() != null) type.setDefaultPrice(request.getDefaultPrice());
        if (request.getIsActive() != null) type.setIsActive(request.getIsActive());
        type = typeRepository.save(type);
        return ResponseEntity.ok(ApiResponse.success("Ticket type updated", toTypeResponse(type)));
    }

    // POST /api/v1/admin/meal-tickets/issue (manual/receptionist)
    @PostMapping("/admin/meal-tickets/issue")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<ApiResponse<MealTicketResponse>> issueManualTicket(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody IssueMealTicketRequest request) {
        Long issuerId = extractUserId(authorizationHeader);
        int validDays = request.getValidDays() != null ? request.getValidDays() : 30;
        MealTicketResponse response = mealTicketService.issueManualTicket(
                request.getUserId(), request.getTicketType(), validDays, issuerId, request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("Meal ticket issued", response));
    }

    // POST /api/v1/groups/{groupId}/meal-tickets/issue (bulk corporate issue)
    @PostMapping("/groups/{groupId}/meal-tickets/issue")
    @PreAuthorize("hasAnyRole('CORPORATE_MEMBER', 'ADMIN')")
    public ResponseEntity<ApiResponse<String>> bulkIssueTickets(
            @PathVariable Long groupId,
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody BulkIssueRequest request) {
        Long issuerId = extractUserId(authorizationHeader);
        int validDays = request.getValidDays() != null ? request.getValidDays() : 30;
        mealTicketService.issueBulkTickets(groupId, request.getTicketType(), validDays,
                issuerId, request.getMemberIds());
        return ResponseEntity.ok(ApiResponse.success(
                request.getMemberIds().size() + " meal tickets issued to group members", null));
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }

    private MealTicketTypeResponse toTypeResponse(MealTicketType type) {
        return MealTicketTypeResponse.builder()
                .typeId(type.getTypeId())
                .code(type.getCode())
                .name(type.getName())
                .description(type.getDescription())
                .defaultValidDays(type.getDefaultValidDays())
                .defaultPrice(type.getDefaultPrice())
                .isActive(type.getIsActive())
                .build();
    }
}
