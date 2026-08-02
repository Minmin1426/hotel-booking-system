package com.hotelbooking.booking.batch;

import com.hotelbooking.booking.batch.dto.ExcelParseResultDto;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BatchBookingServiceTest {

    @Mock private BlockBookingRequestRepository requestRepository;
    @Mock private BlockBookingRowRepository rowRepository;
    @Mock private UserRepository userRepository;
    @Mock private ExcelParsingService excelParsingService;

    @InjectMocks
    private BatchBookingServiceImpl batchBookingService;

    private User testUser;
    private User approverUser;
    private BlockBookingRequest pendingRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder().userId(1L).email("test@hotel.com").build();
        approverUser = User.builder().userId(2L).email("admin@hotel.com").build();
        pendingRequest = BlockBookingRequest.builder()
                .blockBookingId(1L)
                .requester(testUser)
                .status(BatchStatus.PENDING_APPROVAL)
                .fileName("bookings.xlsx")
                .build();
    }

    // ── Upload ────────────────────────────────────────────────────────────────

    @Test
    void uploadExcelFile_InvalidFormat_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "not an xlsx".getBytes());

        assertThrows(Exception.class, () ->
                batchBookingService.uploadExcelFile(1L, file));
    }

    @Test
    void uploadExcelFile_UserNotFound_ThrowsBusinessException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[10]);

        assertThrows(BusinessException.class, () ->
                batchBookingService.uploadExcelFile(999L, file));
    }

    // ── Approve / Reject ────────────────────────────────────────────────────

    @Test
    void approveRequest_ValidRequest_ApprovesAndTriggersProcessing() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(approverUser));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any())).thenReturn(pendingRequest);
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(pendingRequest));

        batchBookingService.approveRequest(2L, 1L);

        assertEquals(BatchStatus.COMPLETED, pendingRequest.getStatus());
        assertEquals(approverUser, pendingRequest.getApprovedBy());
        assertNotNull(pendingRequest.getApprovedAt());
        verify(requestRepository, atLeastOnce()).save(pendingRequest);
    }

    @Test
    void approveRequest_AlreadyApproved_ThrowsException() {
        pendingRequest.setStatus(BatchStatus.APPROVED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(approverUser));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(Exception.class, () ->
                batchBookingService.approveRequest(2L, 1L));
    }

    @Test
    void approveRequest_RequestNotFound_ThrowsException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(approverUser));
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () ->
                batchBookingService.approveRequest(2L, 999L));
    }

    @Test
    void rejectRequest_ValidRequest_RejectsWithReason() {
        pendingRequest.setStatus(BatchStatus.PENDING_APPROVAL);
        when(userRepository.findById(2L)).thenReturn(Optional.of(approverUser));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any())).thenReturn(pendingRequest);

        batchBookingService.rejectRequest(2L, 1L, "Insufficient documentation");

        assertEquals(BatchStatus.REJECTED, pendingRequest.getStatus());
        assertEquals("Insufficient documentation", pendingRequest.getRejectionReason());
        assertEquals(approverUser, pendingRequest.getApprovedBy());
        assertNotNull(pendingRequest.getApprovedAt());
    }

    @Test
    void rejectRequest_NotPending_ThrowsException() {
        pendingRequest.setStatus(BatchStatus.APPROVED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(approverUser));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(Exception.class, () ->
                batchBookingService.rejectRequest(2L, 1L, "reason"));
    }

    // ── Access Control ───────────────────────────────────────────────────────

    @Test
    void getRequestDetails_WrongUser_ThrowsAccessDenied() {
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(pendingRequest));

        assertThrows(BusinessException.class, () ->
                batchBookingService.getRequestDetails(999L, 1L));
    }

    @Test
    void getRequestDetails_CorrectUser_ReturnsDto() {
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(pendingRequest));

        var result = batchBookingService.getRequestDetails(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getBlockBookingId());
        assertEquals("bookings.xlsx", result.getFileName());
    }

    // ── Process ──────────────────────────────────────────────────────────────

    @Test
    void processApprovedRequest_NotApproved_SkipsProcessing() {
        pendingRequest.setStatus(BatchStatus.PENDING_APPROVAL);
        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(pendingRequest));

        // Should not throw, just skip
        assertDoesNotThrow(() -> batchBookingService.processApprovedRequest(1L));
    }

    @Test
    void processApprovedRequest_Approved_ProcessesRows() {
        BlockBookingRow row = BlockBookingRow.builder()
                .rowId(1L)
                .blockBookingRequest(pendingRequest)
                .rowStatus(RowStatus.VALID)
                .guestName("John Doe")
                .build();
        pendingRequest.setStatus(BatchStatus.APPROVED);
        pendingRequest.setRows(java.util.List.of(row));

        when(requestRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(pendingRequest));
        when(requestRepository.save(any())).thenReturn(pendingRequest);

        batchBookingService.processApprovedRequest(1L);

        assertEquals(BatchStatus.COMPLETED, pendingRequest.getStatus());
        verify(rowRepository).saveAll(anyList());
    }
}
