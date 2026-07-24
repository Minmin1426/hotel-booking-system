package com.hotelbooking.mealticket;

import com.hotelbooking.mealticket.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MealTicketService {
    MealTicketResponse issueTicket(Long userId, Long bookingId, String ticketTypeCode, int validDays, Long issuerUserId);
    MealTicketResponse issueManualTicket(Long userId, String ticketTypeCode, int validDays, Long issuerUserId, String notes);
    void issueBulkTickets(Long groupId, String ticketTypeCode, int validDays, Long issuerUserId, java.util.List<Long> memberIds);
    ScanTicketResponse scanAndConsume(String qrCode, Long staffUserId);
    Page<MealTicketResponse> getMyTickets(Long userId, String status, String ticketType, Pageable pageable);
    String getQrImage(Long userId, Long ticketId);
    void expireOldTickets();
}
