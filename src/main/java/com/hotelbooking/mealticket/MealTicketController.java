package com.hotelbooking.mealticket;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.common.security.JwtService;
import com.hotelbooking.mealticket.dto.MealTicketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me/meal-tickets")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class MealTicketController {

    private final MealTicketService mealTicketService;
    private final JwtService jwtService;

    // GET /api/v1/users/me/meal-tickets?status=&type=&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MealTicketResponse>>> getMyTickets(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = extractUserId(authorizationHeader);
        Page<MealTicketResponse> tickets = mealTicketService.getMyTickets(userId, status, type, pageable);
        return ResponseEntity.ok(ApiResponse.success("Meal tickets retrieved", tickets));
    }

    // GET /api/v1/users/me/meal-tickets/{ticketId}/qr
    @GetMapping(value = "/{ticketId}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getQrImage(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long ticketId) {
        Long userId = extractUserId(authorizationHeader);
        String base64 = mealTicketService.getQrImage(userId, ticketId);
        byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }

    private Long extractUserId(String authorizationHeader) {
        String token = authorizationHeader.substring(7);
        return jwtService.extractUserId(token);
    }
}
