package com.hotelbooking.notification;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.notification.dto.NotificationRequest;
import com.hotelbooking.notification.dto.NotificationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/broadcast")
    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'RECEPTIONIST')")
    public ResponseEntity<ApiResponse<NotificationResponse>> broadcast(@Valid @RequestBody NotificationRequest request) {
        log.info("Broadcast notification: {}", request.getTitle());
        return ResponseEntity.ok(ApiResponse.success("Notification queued", notificationService.broadcast(request)));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved", notificationService.listRecent()));
    }
}
