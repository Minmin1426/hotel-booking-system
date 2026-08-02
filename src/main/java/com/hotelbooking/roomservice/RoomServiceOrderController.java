package com.hotelbooking.roomservice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.roomservice.dto.RoomServiceOrderRequest;
import com.hotelbooking.roomservice.dto.RoomServiceOrderResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/room-service")
@RequiredArgsConstructor
@Slf4j
public class RoomServiceOrderController {

    private final List<RoomServiceOrderResponse> orders = new CopyOnWriteArrayList<>();

    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<RoomServiceOrderResponse>> create(@Valid @RequestBody RoomServiceOrderRequest request) {
        RoomServiceOrderResponse response = RoomServiceOrderResponse.builder()
                .id(System.currentTimeMillis())
                .roomNumber(request.getRoomNumber())
                .item(request.getItem())
                .quantity(request.getQuantity())
                .notes(request.getNotes())
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
        orders.add(response);
        log.info("Room service order created for room {}", request.getRoomNumber());
        return ResponseEntity.ok(ApiResponse.success("Order created", response));
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<List<RoomServiceOrderResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved", orders));
    }
}
