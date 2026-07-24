package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.batch.dto.*;
import com.hotelbooking.booking.batch.exception.BatchBookingException;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchBookingServiceImpl implements BatchBookingService {

    private final BlockBookingRequestRepository requestRepository;
    private final BlockBookingRowRepository rowRepository;
    private final UserRepository userRepository;
    private final ExcelParsingService excelParsingService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_FORMATTER_ALT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Transactional
    public ExcelParseResultDto uploadExcelFile(Long userId, MultipartFile file) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".xlsx")) {
            throw new BatchBookingException("INVALID_FILE_FORMAT", "Only .xlsx files are supported");
        }

        // Create request record
        BlockBookingRequest request = BlockBookingRequest.builder()
                .requester(requester)
                .fileName(originalFilename)
                .status(BatchStatus.PENDING_APPROVAL)
                .build();
        request = requestRepository.save(request);

        // Parse and validate Excel
        ExcelParseResultDto parseResult = excelParsingService.parseAndValidate(request.getBlockBookingId(), file);

        // Save rows from Excel
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();
            List<BlockBookingRow> savedRows = new ArrayList<>();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row)) continue;

                BlockBookingRow bookingRow = excelParsingService.parseRow(row, request);

                // Mark as INVALID if it has validation errors
                final int rowNum = i + 1;
                boolean hasError = parseResult.getErrors().stream()
                        .anyMatch(e -> e.getRowNumber() == rowNum && !"file".equals(e.getField()) && !"header".equals(e.getField()));
                if (hasError) {
                    String errorMsg = parseResult.getErrors().stream()
                            .filter(e -> e.getRowNumber() == rowNum)
                            .map(e -> e.getMessage())
                            .reduce((a, b) -> a + "; " + b)
                            .orElse("Validation failed");
                    bookingRow.markInvalid(errorMsg);
                }

                savedRows.add(bookingRow);
            }

            rowRepository.saveAll(savedRows);

            // Update request totals
            request.setTotalGuests(savedRows.size());
            long invalidCount = savedRows.stream()
                    .filter(r -> r.getRowStatus() == RowStatus.INVALID)
                    .count();
            if (invalidCount > 0) {
                request.setStatus(BatchStatus.FAILED);
            }
            requestRepository.save(request);

            parseResult.setBlockBookingId(request.getBlockBookingId());
            parseResult.setInvalidRows((int) invalidCount);
            parseResult.setValidRows(savedRows.size() - (int) invalidCount);

        } catch (IOException e) {
            log.error("Failed to read Excel file", e);
            throw new BatchBookingException("FILE_READ_ERROR", "Failed to read Excel file");
        }

        return parseResult;
    }

    @Override
    public Page<BlockBookingResponseDto> getMyRequests(Long userId, Pageable pageable) {
        return requestRepository.findByRequesterUserId(userId, pageable)
                .map(this::toDto);
    }

    @Override
    public BlockBookingResponseDto getRequestDetails(Long userId, Long blockBookingId) {
        BlockBookingRequest request = requestRepository.findByIdWithDetails(blockBookingId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "Block booking request not found"));

        if (!request.getRequester().getUserId().equals(userId)) {
            throw new BusinessException("ACCESS_DENIED", "You do not have access to this request");
        }

        return toDto(request);
    }

    @Override
    public Page<BlockBookingResponseDto> getAllRequests(Pageable pageable) {
        return requestRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public BlockBookingResponseDto getAllRequestDetails(Long blockBookingId) {
        BlockBookingRequest request = requestRepository.findByIdWithDetails(blockBookingId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "Block booking request not found"));
        return toDto(request);
    }

    @Override
    @Transactional
    public void approveRequest(Long approverId, Long blockBookingId) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Approver not found"));

        BlockBookingRequest request = requestRepository.findById(blockBookingId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "Block booking request not found"));

        if (request.getStatus() != BatchStatus.PENDING_APPROVAL) {
            throw new BatchBookingException("INVALID_STATUS", "Request is not pending approval");
        }

        request.approve(approver);
        requestRepository.save(request);

        // Trigger async processing
        processApprovedRequestAsync(blockBookingId);
    }

    @Override
    @Transactional
    public void rejectRequest(Long approverId, Long blockBookingId, String reason) {
        User approver = userRepository.findById(approverId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Approver not found"));

        BlockBookingRequest request = requestRepository.findById(blockBookingId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "Block booking request not found"));

        if (request.getStatus() != BatchStatus.PENDING_APPROVAL) {
            throw new BatchBookingException("INVALID_STATUS", "Request is not pending approval");
        }

        request.reject(approver, reason);
        requestRepository.save(request);
    }

    @Override
    @Transactional
    public void processApprovedRequest(Long blockBookingId) {
        BlockBookingRequest request = requestRepository.findByIdWithDetails(blockBookingId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "Block booking request not found"));

        if (request.getStatus() != BatchStatus.APPROVED) {
            log.warn("Cannot process request {} - status is {}", blockBookingId, request.getStatus());
            return;
        }

        request.setStatus(BatchStatus.PROCESSING);
        requestRepository.save(request);

        // Note: Actual booking creation would need access to HotelRepository, RoomTypeRepository,
        // BookingService, PaymentService, etc. For now, we mark rows as processed.
        // In a full implementation, this would:
        // 1. For each valid row, call BookingService to create individual bookings
        // 2. Handle payment processing
        // 3. Update row status to BOOKED or FAILED

        List<BlockBookingRow> rows = request.getRows();
        int successCount = 0;
        int failCount = 0;

        for (BlockBookingRow row : rows) {
            if (row.getRowStatus() != RowStatus.VALID) continue;

            try {
                // TODO: Actual booking creation logic here
                // This would need to:
                // 1. Find available room for the hotel + room type + dates
                // 2. Create a Booking entity
                // 3. Process payment (or mark as pending payment for corporate accounts)
                // 4. Mark row as BOOKED with booking reference
                row.setRowStatus(RowStatus.BOOKED);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create booking for row {}", row.getRowId(), e);
                row.markFailed("Booking creation failed: " + e.getMessage());
                failCount++;
            }
        }

        rowRepository.saveAll(rows);

        // Update request status
        if (failCount == 0) {
            request.setStatus(BatchStatus.COMPLETED);
        } else if (successCount > 0) {
            request.setStatus(BatchStatus.PARTIAL_SUCCESS);
        } else {
            request.setStatus(BatchStatus.FAILED);
        }

        requestRepository.save(request);
        log.info("Processed block booking request {}: {} success, {} failed", blockBookingId, successCount, failCount);
    }

    @Async
    private void processApprovedRequestAsync(Long blockBookingId) {
        processApprovedRequest(blockBookingId);
    }

    private BlockBookingResponseDto toDto(BlockBookingRequest request) {
        List<BlockBookingRowDto> rowDtos = request.getRows().stream()
                .map(this::toRowDto)
                .toList();

        return BlockBookingResponseDto.builder()
                .blockBookingId(request.getBlockBookingId())
                .fileName(request.getFileName())
                .totalGuests(request.getTotalGuests())
                .totalAmount(request.getTotalAmount())
                .status(request.getStatus().name())
                .rejectionReason(request.getRejectionReason())
                .requesterId(request.getRequester() != null ? request.getRequester().getUserId() : null)
                .requesterEmail(request.getRequester() != null ? request.getRequester().getEmail() : null)
                .approvedBy(request.getApprovedBy() != null ? request.getApprovedBy().getUserId() : null)
                .approvedAt(request.getApprovedAt())
                .createdAt(request.getCreatedAt())
                .rows(rowDtos)
                .build();
    }

    private BlockBookingRowDto toRowDto(BlockBookingRow row) {
        return BlockBookingRowDto.builder()
                .rowId(row.getRowId())
                .guestName(row.getGuestName())
                .email(row.getEmail())
                .phoneNumber(row.getPhoneNumber())
                .hotelId(row.getHotel() != null ? row.getHotel().getHotelId() : null)
                .hotelName(row.getHotel() != null ? row.getHotel().getName() : null)
                .checkInDate(row.getCheckInDate())
                .checkOutDate(row.getCheckOutDate())
                .roomType(row.getRoomType())
                .quantity(row.getQuantity())
                .bookingId(row.getBooking() != null ? row.getBooking().getBookingId() : null)
                .rowStatus(row.getRowStatus().name())
                .errorMessage(row.getErrorMessage())
                .build();
    }

    private boolean isEmptyRow(Row row) {
        if (row == null) return true;
        for (int i = 0; i < 8; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String val = getCellValueAsString(cell).trim();
                if (!val.isEmpty()) return false;
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }
}
