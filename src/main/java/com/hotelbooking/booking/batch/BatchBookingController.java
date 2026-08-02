package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.batch.dto.BlockBookingResponseDto;
import com.hotelbooking.booking.batch.dto.ExcelParseResultDto;
import com.hotelbooking.common.dto.ApiResponse;
import com.hotelbooking.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users/me/block-bookings")
@RequiredArgsConstructor
public class BatchBookingController {

    private final BatchBookingService batchBookingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ExcelParseResultDto>> uploadExcel(
            @CurrentUser Long userId,
            @RequestParam("file") MultipartFile file) {
        ExcelParseResultDto result = batchBookingService.uploadExcelFile(userId, file);
        return ResponseEntity.ok(ApiResponse.success("Excel file uploaded successfully", result));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<BlockBookingResponseDto>>> getMyRequests(
            @CurrentUser Long userId,
            Pageable pageable) {
        Page<BlockBookingResponseDto> requests = batchBookingService.getMyRequests(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success("Block booking requests retrieved", requests));
    }

    @GetMapping("/{blockBookingId}")
    public ResponseEntity<ApiResponse<BlockBookingResponseDto>> getRequestDetails(
            @CurrentUser Long userId,
            @PathVariable Long blockBookingId) {
        BlockBookingResponseDto request = batchBookingService.getRequestDetails(userId, blockBookingId);
        return ResponseEntity.ok(ApiResponse.success("Block booking request details", request));
    }
}
