package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.batch.dto.BlockBookingResponseDto;
import com.hotelbooking.booking.batch.dto.ExcelParseResultDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface BatchBookingService {

    ExcelParseResultDto uploadExcelFile(Long userId, MultipartFile file);

    Page<BlockBookingResponseDto> getMyRequests(Long userId, Pageable pageable);

    BlockBookingResponseDto getRequestDetails(Long userId, Long blockBookingId);

    Page<BlockBookingResponseDto> getAllRequests(Pageable pageable);

    BlockBookingResponseDto getAllRequestDetails(Long blockBookingId);

    void approveRequest(Long approverId, Long blockBookingId);

    void rejectRequest(Long approverId, Long blockBookingId, String reason);

    void processApprovedRequest(Long blockBookingId);
}
