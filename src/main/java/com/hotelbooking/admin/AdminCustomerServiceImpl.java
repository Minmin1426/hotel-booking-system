package com.hotelbooking.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.admin.dto.*;
import com.hotelbooking.booking.Booking;
import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.loyalty.LoyaltyPointLedgerRepository;
import com.hotelbooking.mealticket.MealTicketRepository;
import com.hotelbooking.mealticket.TicketStatus;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.voucher.UserVoucherRepository;
import com.hotelbooking.wallet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WalletRepository walletRepository;
    private final MealTicketRepository mealTicketRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final LoyaltyPointLedgerRepository loyaltyPointLedgerRepository;
    private final CustomerNoteRepository customerNoteRepository;
    private final CustomerActivityEventRepository activityEventRepository;
    private final BulkActionLogRepository bulkActionLogRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final CustomerActivityRecorder activityRecorder;
    private final ObjectMapper objectMapper;

    // ── FR-001: List customers ───────────────────────────────────────────────

    @Override
    public Page<CustomerListItem> listCustomers(CustomerSpecification spec, Pageable pageable) {
        Page<User> users = userRepository.findAll(spec.toSpecification(), pageable);
        return users.map(this::toListItem);
    }

    // ── FR-002: 360° detail ─────────────────────────────────────────────────

    @Override
    public CustomerDetail360Response getCustomerDetail360(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));

        // Wallet
        BigDecimal walletBalance = walletRepository
                .findByOwnerUserUserIdAndWalletTypeAndGroupIsNull(userId, WalletType.PERSONAL)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        BigDecimal groupBalance = null;
        if (user.isCorporateMember()) {
            groupBalance = walletRepository
                    .findByOwnerUserUserIdAndWalletType(userId, WalletType.GROUP)
                    .map(Wallet::getBalance)
                    .orElse(null);
        }

        // Counts
        long bookingCount = bookingRepository.countByUser_UserId(userId);
        long activeMealTickets = mealTicketRepository
                .countByUserUserIdAndStatus(userId, TicketStatus.UNUSED);
        long claimedVouchers = userVoucherRepository
                .findByUserUserIdOrderByClaimedAtDesc(userId, PageRequest.of(0, 1))
                .getTotalElements();
        long pinnedNotes = customerNoteRepository.countPinnedByUserId(userId);

        // Loyalty points
        Long loyaltyPoints = loyaltyPointLedgerRepository
                .findFirstByUserUserIdOrderByCreatedAtDesc(userId)
                .map(lp -> lp.getRunningBalance())
                .orElse(0L);

        // Recent bookings
        List<BookingSummaryDto> recentBookings = bookingRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5))
                .getContent().stream()
                .map(this::toBookingSummary)
                .toList();

        // Recent activity
        List<CustomerActivityEventResponse> recentActivity = activityEventRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 10))
                .getContent().stream()
                .map(this::toActivityResponse)
                .toList();

        // Event type breakdown
        Object[] typeCounts = activityEventRepository.getEventTypeCounts(userId);
        Map<String, Long> eventBreakdown = new HashMap<>();
        if (typeCounts != null) {
            for (Object row : typeCounts) {
                if (row instanceof Object[] arr && arr.length == 2) {
                    eventBreakdown.put(String.valueOf(arr[0]), ((Number) arr[1]).longValue());
                }
            }
        }

        return CustomerDetail360Response.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .identificationNumber(user.getIdentificationNumber())
                .role(user.getRole())
                .status(user.getStatus())
                .accountType(user.getAccountType())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .currentTier(user.getCurrentTier())
                .tierEvaluatedAt(user.getTierEvaluatedAt())
                .lifetimeSpend(BigDecimal.ZERO) // TODO: sum from payment history
                .totalBookings(bookingCount)
                .loyaltyPoints(loyaltyPoints)
                .ctpStatus(user.getCtpStatus())
                .companyName(user.getCompanyName())
                .taxCode(user.getTaxCode())
                .ctpVerifiedAt(user.getCtpVerifiedAt())
                .walletBalance(walletBalance)
                .groupWalletBalance(groupBalance)
                .isVip(user.getIsVip())
                .vipMarkedAt(user.getVipMarkedAt())
                .vipMarkedBy(user.getVipMarkedBy())
                .activeMealTickets(activeMealTickets)
                .claimedVouchers(claimedVouchers)
                .pinnedNotes(pinnedNotes)
                .recentBookings(recentBookings)
                .recentActivity(recentActivity)
                .eventTypeBreakdown(eventBreakdown)
                .build();
    }

    // ── FR-003: VIP status ─────────────────────────────────────────────────

    @Override
    @Transactional
    public VipStatusResponse setVipStatus(Long userId, Long adminId, Boolean isVip) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));

        boolean wasVip = Boolean.TRUE.equals(user.getIsVip());
        user.setIsVip(isVip);
        user.setVipMarkedAt(isVip ? LocalDateTime.now() : null);
        user.setVipMarkedBy(isVip ? adminId : null);
        userRepository.save(user);

        activityRecorder.recordVipMarked(userId, isVip, adminId);

        return VipStatusResponse.builder()
                .userId(user.getUserId())
                .isVip(user.getIsVip())
                .vipMarkedAt(user.getVipMarkedAt())
                .vipMarkedBy(user.getVipMarkedBy())
                .build();
    }

    // ── FR-004: Notes ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public CustomerNoteResponse addNote(Long userId, Long authorId, CustomerNoteRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Author not found"));

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException("INVALID_NOTE_CONTENT", "Note content cannot be empty");
        }

        CustomerNote note = CustomerNote.builder()
                .user(user)
                .author(author)
                .content(request.getContent().trim())
                .isPinned(Boolean.TRUE.equals(request.getIsPinned()))
                .build();

        note = customerNoteRepository.save(note);
        return toNoteResponse(note);
    }

    @Override
    @Transactional
    public CustomerNoteResponse updateNote(Long noteId, Long userId, Long authorId, CustomerNoteRequest request) {
        CustomerNote note = customerNoteRepository.findByNoteIdAndUserUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("NOTE_NOT_FOUND", "Note not found"));

        if (request.getContent() == null || request.getContent().isBlank()) {
            throw new BusinessException("INVALID_NOTE_CONTENT", "Note content cannot be empty");
        }

        note.setContent(request.getContent().trim());
        if (request.getIsPinned() != null) {
            note.setIsPinned(request.getIsPinned());
        }
        note = customerNoteRepository.save(note);
        return toNoteResponse(note);
    }

    @Override
    @Transactional
    public void deleteNote(Long noteId, Long userId) {
        CustomerNote note = customerNoteRepository.findByNoteIdAndUserUserId(noteId, userId)
                .orElseThrow(() -> new BusinessException("NOTE_NOT_FOUND", "Note not found"));
        customerNoteRepository.delete(note);
    }

    @Override
    public Page<CustomerNoteResponse> getNotes(Long userId, Pageable pageable) {
        return customerNoteRepository
                .findByUserUserIdOrderByIsPinnedDescCreatedAtDesc(userId, pageable)
                .map(this::toNoteResponse);
    }

    // ── FR-006: Activity timeline ───────────────────────────────────────────

    @Override
    public Page<CustomerActivityEventResponse> getActivityTimeline(Long userId, String eventType, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("CUSTOMER_NOT_FOUND", "Customer not found"));

        Page<CustomerActivityEvent> events;
        if (eventType != null && !eventType.isBlank()) {
            events = activityEventRepository.findByUserUserIdAndEventTypeOrderByCreatedAtDesc(userId, eventType, pageable);
        } else {
            events = activityEventRepository.findByUserUserIdOrderByCreatedAtDesc(userId, pageable);
        }
        return events.map(this::toActivityResponse);
    }

    // ── FR-007: Statistics ─────────────────────────────────────────────────

    @Override
    public CustomerStatsResponse getStats() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();

        List<User> allUsers = userRepository.findAll();

        // Exclude admins
        List<User> customers = allUsers.stream()
                .filter(u -> !u.getRole().equals("ADMIN") && !u.getRole().equals("DIRECTOR"))
                .toList();

        long total = customers.size();
        long active = customers.stream()
                .filter(u -> u.getLastLoginAt() != null && u.getLastLoginAt().isAfter(thirtyDaysAgo))
                .count();
        long newThisMonth = customers.stream()
                .filter(u -> u.getCreatedAt() != null && u.getCreatedAt().isAfter(monthStart))
                .count();
        long vipCount = customers.stream()
                .filter(u -> Boolean.TRUE.equals(u.getIsVip()))
                .count();

        Map<String, Long> byTier = customers.stream()
                .collect(Collectors.groupingBy(u -> u.getCurrentTier() != null ? u.getCurrentTier() : "NONE", Collectors.counting()));
        Map<String, Long> byAccountType = customers.stream()
                .collect(Collectors.groupingBy(u -> u.getAccountType() != null ? u.getAccountType() : "NONE", Collectors.counting()));
        Map<String, Long> byStatus = customers.stream()
                .collect(Collectors.groupingBy(u -> u.getStatus() != null ? u.getStatus() : "UNKNOWN", Collectors.counting()));

        return CustomerStatsResponse.builder()
                .totalCustomers(total)
                .activeCustomers(active)
                .newCustomersThisMonth(newThisMonth)
                .vipCount(vipCount)
                .byTier(byTier)
                .byAccountType(byAccountType)
                .byStatus(byStatus)
                .build();
    }

    // ── FR-005: Bulk actions ───────────────────────────────────────────────

    @Override
    @Transactional
    public BulkActionResponse executeBulkAction(Long adminId, BulkActionRequest request) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Admin not found"));

        List<Long> customerIds = request.getCustomerIds();
        if (customerIds == null || customerIds.isEmpty()) {
            throw new BusinessException("NO_CUSTOMERS_SELECTED", "At least one customer must be selected");
        }

        String action = request.getAction();
        if (action == null || action.isBlank()) {
            throw new BusinessException("INVALID_BULK_ACTION", "Action is required");
        }

        // Log the bulk action
        String targetIds = customerIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String payloadJson = null;
        if (request.getPayload() != null) {
            try {
                payloadJson = objectMapper.writeValueAsString(request.getPayload());
            } catch (Exception ignored) {}
        }

        BulkActionLog logEntry = BulkActionLog.builder()
                .admin(admin)
                .actionType(action)
                .targetUserIds(targetIds)
                .payload(payloadJson)
                .build();
        final BulkActionLog savedLog = bulkActionLogRepository.save(logEntry);

        int affected = 0;
        String message;

        switch (action.toUpperCase()) {
            case "LOCK_ACCOUNTS" -> {
                for (Long customerId : customerIds) {
                    userRepository.findById(customerId).ifPresent(u -> {
                        u.setStatus("LOCKED");
                        userRepository.save(u);
                        activityRecorder.record(customerId, "ACCOUNT_LOCKED",
                                "Account locked by admin " + adminId,
                                Map.of("bulkActionId", savedLog.getBulkActionId()),
                                adminId);
                    });
                    affected++;
                }
                message = affected + " accounts locked";
            }
            case "UNLOCK_ACCOUNTS" -> {
                for (Long customerId : customerIds) {
                    userRepository.findById(customerId).ifPresent(u -> {
                        u.setStatus("ACTIVE");
                        userRepository.save(u);
                    });
                    affected++;
                }
                message = affected + " accounts unlocked";
            }
            case "SEND_NOTIFICATION" -> {
                // TODO: Integrate with notification/email service
                affected = customerIds.size();
                message = "Notification queued for " + affected + " customers";
            }
            case "APPLY_TIER" -> {
                if (request.getPayload() == null || !request.getPayload().containsKey("tier")) {
                    throw new BusinessException("INVALID_BULK_ACTION", "APPLY_TIER requires 'tier' in payload");
                }
                String tier = String.valueOf(request.getPayload().get("tier"));
                for (Long customerId : customerIds) {
                    userRepository.findById(customerId).ifPresent(u -> {
                        String previousTier = u.getCurrentTier();
                        u.setCurrentTier(tier);
                        userRepository.save(u);
                        activityRecorder.recordTierChanged(customerId, previousTier, tier, adminId);
                    });
                    affected++;
                }
                message = affected + " customers upgraded to " + tier;
            }
            default -> throw new BusinessException("INVALID_BULK_ACTION", "Unknown action: " + action);
        }

        return BulkActionResponse.builder()
                .bulkActionId(savedLog.getBulkActionId())
                .actionType(action)
                .affectedCount(affected)
                .message(message)
                .build();
    }

    // ── FR-009: Group members ──────────────────────────────────────────────

    @Override
    public Page<GroupMemberListItem> getGroupMembers(Long groupId, Long ownerId, String search, Pageable pageable) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException("GROUP_NOT_FOUND", "Group not found"));

        // Ownership check
        if (!group.getOwnerUser().getUserId().equals(ownerId)) {
            throw new BusinessException("ACCESS_DENIED", "You do not own this group");
        }

        CustomerSpecification spec = CustomerSpecification.builder()
                .groupId(groupId)
                .search(search)
                .build();

        Page<User> members = userRepository.findAll(spec.toSpecification(), pageable);
        return members.map(this::toGroupMemberItem);
    }

    // ── Mappers ─────────────────────────────────────────────────────────────

    private CustomerListItem toListItem(User u) {
        BigDecimal balance = walletRepository
                .findByOwnerUserUserIdAndWalletTypeAndGroupIsNull(u.getUserId(), WalletType.PERSONAL)
                .map(Wallet::getBalance)
                .orElse(BigDecimal.ZERO);

        long bookings = bookingRepository.countByUser_UserId(u.getUserId());

        return CustomerListItem.builder()
                .userId(u.getUserId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole())
                .status(u.getStatus())
                .accountType(u.getAccountType())
                .currentTier(u.getCurrentTier())
                .isVip(u.getIsVip())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .walletBalance(balance)
                .totalBookings(bookings)
                .build();
    }

    private BookingSummaryDto toBookingSummary(Booking b) {
        return BookingSummaryDto.builder()
                .bookingId(b.getBookingId())
                .status(b.getStatus())
                .checkInDate(b.getCheckInDate() != null ? b.getCheckInDate().toLocalDate() : null)
                .checkOutDate(b.getCheckOutDate() != null ? b.getCheckOutDate().toLocalDate() : null)
                .hotelName(b.getHotel() != null ? b.getHotel().getName() : null)
                .totalPrice(b.getTotalAmount())
                .createdAt(b.getCreatedAt() != null ? b.getCreatedAt().toString() : null)
                .build();
    }

    private CustomerActivityEventResponse toActivityResponse(CustomerActivityEvent e) {
        return CustomerActivityEventResponse.builder()
                .eventId(e.getEventId())
                .eventType(e.getEventType())
                .eventSummary(e.getEventSummary())
                .eventMetadata(e.getEventMetadata())
                .actorUserId(e.getActor() != null ? e.getActor().getUserId() : null)
                .actorName(e.getActor() != null ? e.getActor().getFullName() : null)
                .createdAt(e.getCreatedAt())
                .build();
    }

    private CustomerNoteResponse toNoteResponse(CustomerNote note) {
        return CustomerNoteResponse.builder()
                .noteId(note.getNoteId())
                .userId(note.getUser().getUserId())
                .authorId(note.getAuthor().getUserId())
                .authorName(note.getAuthor().getFullName())
                .content(note.getContent())
                .isPinned(note.getIsPinned())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }

    private GroupMemberListItem toGroupMemberItem(User u) {
        return GroupMemberListItem.builder()
                .userId(u.getUserId())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phoneNumber(u.getPhoneNumber())
                .status(u.getStatus())
                .currentTier(u.getCurrentTier())
                .lastLoginAt(u.getLastLoginAt())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
