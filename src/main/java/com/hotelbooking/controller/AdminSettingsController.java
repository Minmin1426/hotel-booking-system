package com.hotelbooking.controller;

import com.hotelbooking.dto.ApiResponse;
import com.hotelbooking.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/settings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Slf4j
public class AdminSettingsController {

    private final SystemSettingService systemSettingService;

    @GetMapping("/lock-duration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getLockDuration() {
        int duration = systemSettingService.getLockDurationMinutes();
        log.info("Admin requested room lock duration: {} minutes", duration);
        return ResponseEntity.ok(ApiResponse.success(
            "Lock duration retrieved successfully", 
            Map.of("lockDurationMinutes", duration)
        ));
    }

    @PostMapping("/lock-duration")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setLockDuration(@RequestBody Map<String, Object> payload) {
        Object durationObj = payload.get("lockDurationMinutes");
        if (durationObj == null) {
            throw new IllegalArgumentException("lockDurationMinutes is required");
        }
        int duration = ((Number) durationObj).intValue();
        systemSettingService.setLockDurationMinutes(duration);
        log.info("Admin updated room lock auto-release duration to {} minutes", duration);
        return ResponseEntity.ok(ApiResponse.success(
            "Lock duration updated successfully", 
            Map.of("lockDurationMinutes", duration)
        ));
    }
}
