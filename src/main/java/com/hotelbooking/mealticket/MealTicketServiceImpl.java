package com.hotelbooking.mealticket;

import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.mealticket.dto.*;
import com.hotelbooking.mealticket.exception.*;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.wallet.GroupMembership;
import com.hotelbooking.wallet.GroupMembershipRepository;
import com.hotelbooking.wallet.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MealTicketServiceImpl implements MealTicketService {

    private final MealTicketRepository ticketRepository;
    private final MealTicketTypeRepository typeRepository;
    private final MealTicketAuditLogRepository auditRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final com.hotelbooking.admin.CustomerActivityRecorder activityRecorder;

    // ── AC-061: Issue ticket (booking auto-issue or loyalty tier benefit) ─────────

    @Override
    @Transactional
    public MealTicketResponse issueTicket(Long userId, Long bookingId, String ticketTypeCode,
                                          int validDays, Long issuerUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        MealTicketType ticketType = typeRepository.findByCodeAndIsActiveTrue(ticketTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("MealTicketType", "code", ticketTypeCode));
        Booking booking = bookingId != null ?
                bookingRepository.findById(bookingId).orElse(null) : null;
        User issuer = issuerUserId != null ?
                userRepository.findById(issuerUserId).orElse(null) : null;

        return createTicket(user, booking, ticketType, validDays, issuer, null, "ISSUED");
    }

    @Override
    @Transactional
    public MealTicketResponse issueManualTicket(Long userId, String ticketTypeCode,
                                                int validDays, Long issuerUserId, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId.toString()));
        MealTicketType ticketType = typeRepository.findByCodeAndIsActiveTrue(ticketTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("MealTicketType", "code", ticketTypeCode));
        User issuer = userRepository.findById(issuerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", issuerUserId.toString()));

        return createTicket(user, null, ticketType, validDays, issuer, notes, "ISSUED");
    }

    // ── AC-065: Bulk issue to corporate group members ──────────────────────────

    @Override
    @Transactional
    public void issueBulkTickets(Long groupId, String ticketTypeCode, int validDays,
                                  Long issuerUserId, List<Long> memberIds) {
        User issuer = userRepository.findById(issuerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", issuerUserId.toString()));
        MealTicketType ticketType = typeRepository.findByCodeAndIsActiveTrue(ticketTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException("MealTicketType", "code", ticketTypeCode));

        for (Long memberId : memberIds) {
            User member = userRepository.findById(memberId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", memberId.toString()));

            // Verify member belongs to the group
            membershipRepository.findByGroupIdAndMemberUserId(groupId, memberId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User " + memberId + " is not a member of group " + groupId));

            createTicket(member, null, ticketType, validDays, issuer,
                    "Bulk issue for group " + groupId, "ISSUED");
        }

        log.info("Bulk issued {} tickets of type {} to group {} by issuer {}",
                memberIds.size(), ticketTypeCode, groupId, issuerUserId);
    }

    // ── AC-063: Staff scans QR to consume ───────────────────────────────────────

    @Override
    @Transactional
    public ScanTicketResponse scanAndConsume(String qrCode, Long staffUserId) {
        // Step 1: Verify QR signature — throws SecurityException → convert to InvalidQrCodeException
        QrCodeGenerator.QrPayload payload;
        try {
            payload = qrCodeGenerator.verifyQr(qrCode);
        } catch (SecurityException e) {
            throw new InvalidQrCodeException(e.getMessage());
        }

        // Step 2: Find the ticket
        MealTicket ticket = ticketRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new TicketNotFoundException("MealTicket not found for QR code"));

        // Step 3: Validate ownership
        if (!ticket.getUser().getUserId().equals(payload.userId())) {
            throw new InvalidQrCodeException("INVALID_QR_CODE: Ticket does not belong to scanned user");
        }

        // Step 4: Check status
        if (ticket.getStatus() == TicketStatus.USED) {
            throw new TicketAlreadyUsedException("TICKET_ALREADY_USED: Ticket was already consumed");
        }
        if (ticket.getStatus() == TicketStatus.EXPIRED) {
            throw new TicketExpiredException("TICKET_EXPIRED: Ticket has expired");
        }
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new TicketExpiredException("TICKET_CANCELLED: Ticket has been cancelled");
        }

        // Step 5: Check expiry time (belt-and-suspenders)
        if (ticket.isExpired()) {
            ticket.markExpired();
            ticketRepository.save(ticket);
            throw new TicketExpiredException("TICKET_EXPIRED: Ticket has expired");
        }

        // Step 6: Consume
        User staff = userRepository.findById(staffUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", staffUserId.toString()));
        ticket.consume(staff);
        ticketRepository.save(ticket);

        // AC-067: Audit log
        MealTicketAuditLog audit = MealTicketAuditLog.builder()
                .ticket(ticket)
                .action("SCANNED")
                .actorUser(staff)
                .metadata("{ \"staffId\": " + staffUserId + " }")
                .build();
        auditRepository.save(audit);

        log.info("Ticket {} consumed by staff {} for user {}", ticket.getTicketId(), staffUserId, payload.userId());

        // 015-admin-customer-management: Record meal ticket scan activity
        activityRecorder.recordMealTicketScanned(ticket.getUser().getUserId(), ticket.getTicketType().getCode());

        return ScanTicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUser().getUserId())
                .userFullName(ticket.getUser().getFullName())
                .ticketType(ticket.getTicketType().getCode())
                .status("USED")
                .consumedAt(ticket.getUsedAt())
                .build();
    }

    // ── AC-061: Get user's tickets ───────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<MealTicketResponse> getMyTickets(Long userId, String status, String ticketType, Pageable pageable) {
        Page<MealTicket> page;
        if (status != null && !status.isBlank() && ticketType != null && !ticketType.isBlank()) {
            TicketStatus ts = TicketStatus.valueOf(status.toUpperCase());
            page = ticketRepository.findByUserUserIdAndStatusAndTicketTypeCodeOrderByIssuedAtDesc(
                    userId, ts, ticketType, pageable);
        } else if (status != null && !status.isBlank()) {
            TicketStatus ts = TicketStatus.valueOf(status.toUpperCase());
            page = ticketRepository.findByUserUserIdAndStatusOrderByIssuedAtDesc(userId, ts, pageable);
        } else if (ticketType != null && !ticketType.isBlank()) {
            page = ticketRepository.findByUserUserIdAndTicketTypeCodeOrderByIssuedAtDesc(
                    userId, ticketType, pageable);
        } else {
            page = ticketRepository.findByUserUserIdOrderByIssuedAtDesc(userId, pageable);
        }
        return page.map(this::toResponse);
    }

    // ── QR image ─────────────────────────────────────────────────────────────────

    @Override
    public String getQrImage(Long userId, Long ticketId) {
        MealTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("MealTicket", "id", ticketId.toString()));

        if (!ticket.getUser().getUserId().equals(userId)) {
            throw new TicketNotFoundException("ACCESS_DENIED: You do not own this ticket");
        }

        return qrCodeGenerator.regenerateQrImage(ticket.getQrCode());
    }

    // ── Daily expiry job ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void expireOldTickets() {
        int count = ticketRepository.expireOldTickets(LocalDateTime.now());
        log.info("Expired {} old meal tickets", count);
    }

    // ── Internal helpers ────────────────────────────────────────────────────────

    private MealTicketResponse createTicket(User user, Booking booking, MealTicketType ticketType,
                                            int validDays, User issuer, String notes, String action) {
        int days = validDays > 0 ? validDays : ticketType.getDefaultValidDays();

        // Generate UUID as ticketId for QR — must be stable before signing
        String ticketUuid = java.util.UUID.randomUUID().toString();

        QrCodeGenerator.QrResult qrResult = qrCodeGenerator.generateQr(
                (long) (ticketUuid.hashCode() & 0x7FFFFFFF), user.getUserId());

        MealTicket ticket = MealTicket.builder()
                .user(user)
                .booking(booking)
                .ticketType(ticketType)
                .qrCode(qrResult.payload())
                .qrSignature(qrResult.signature())
                .status(TicketStatus.UNUSED)
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(days))
                .issuedBy(issuer)
                .notes(notes)
                .build();

        ticket = ticketRepository.save(ticket);

        // AC-067: Audit log
        MealTicketAuditLog audit = MealTicketAuditLog.builder()
                .ticket(ticket)
                .action(action)
                .actorUser(issuer)
                .metadata("{ \"bookingId\": " + (booking != null ? booking.getBookingId() : "null") +
                        ", \"ticketType\": \"" + ticketType.getCode() + "\" }")
                .build();
        auditRepository.save(audit);

        log.info("MealTicket issued: id={}, type={}, user={}, expires={}",
                ticket.getTicketId(), ticketType.getCode(), user.getUserId(), ticket.getExpiresAt());

        return toResponse(ticket);
    }

    private MealTicketResponse toResponse(MealTicket ticket) {
        return MealTicketResponse.builder()
                .ticketId(ticket.getTicketId())
                .userId(ticket.getUser().getUserId())
                .userFullName(ticket.getUser().getFullName())
                .bookingId(ticket.getBooking() != null ? ticket.getBooking().getBookingId() : null)
                .ticketType(ticket.getTicketType().getCode())
                .ticketTypeName(ticket.getTicketType().getName())
                .qrCode(ticket.getQrCode())
                .status(ticket.getStatus().name())
                .issuedAt(ticket.getIssuedAt())
                .expiresAt(ticket.getExpiresAt())
                .usedAt(ticket.getUsedAt())
                .consumedByStaffName(ticket.getConsumedByStaff() != null ?
                        ticket.getConsumedByStaff().getFullName() : null)
                .issuedByUserId(ticket.getIssuedBy() != null ? ticket.getIssuedBy().getUserId() : null)
                .notes(ticket.getNotes())
                .build();
    }
}
