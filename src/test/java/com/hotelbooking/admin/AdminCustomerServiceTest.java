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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCustomerServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private MealTicketRepository mealTicketRepository;
    @Mock private UserVoucherRepository userVoucherRepository;
    @Mock private LoyaltyPointLedgerRepository loyaltyPointLedgerRepository;
    @Mock private CustomerNoteRepository customerNoteRepository;
    @Mock private CustomerActivityEventRepository activityEventRepository;
    @Mock private BulkActionLogRepository bulkActionLogRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMembershipRepository groupMembershipRepository;
    @Mock private CustomerActivityRecorder activityRecorder;

    @InjectMocks
    private AdminCustomerServiceImpl adminService;

    private User testCustomer;
    private User testAdmin;
    private Wallet testWallet;

    @BeforeEach
    void setUp() {
        testCustomer = User.builder()
                .userId(1L).email("customer@test.com").fullName("Test Customer")
                .role("CUSTOMER").status("ACTIVE").accountType("CUSTOMER")
                .currentTier("GOLD").isVip(false)
                .createdAt(LocalDateTime.now().minusMonths(3))
                .lastLoginAt(LocalDateTime.now().minusDays(5))
                .build();

        testAdmin = User.builder()
                .userId(2L).email("admin@hotel.com").fullName("Admin User")
                .role("ADMIN").status("ACTIVE").build();

        testWallet = Wallet.builder()
                .walletId(1L).ownerUser(testCustomer)
                .walletType(WalletType.PERSONAL).balance(new BigDecimal("500.00"))
                .status(WalletStatus.ACTIVE).build();
    }

    // ── FR-001: List customers ───────────────────────────────────────────────

    @Test
    void listCustomers_ReturnsPagedResults() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testCustomer));
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(walletRepository.findByOwnerUserUserIdAndWalletTypeAndGroupIsNull(1L, WalletType.PERSONAL))
                .thenReturn(Optional.of(testWallet));
        when(bookingRepository.countByUser_UserId(1L)).thenReturn(5L);

        Page<CustomerListItem> result = adminService.listCustomers(
                CustomerSpecification.builder().build(), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("customer@test.com", result.getContent().get(0).getEmail());
        assertEquals(new BigDecimal("500.00"), result.getContent().get(0).getWalletBalance());
        assertEquals(5L, result.getContent().get(0).getTotalBookings());
    }

    @Test
    void listCustomers_WithVipFilter_ReturnsOnlyVipCustomers() {
        testCustomer.setIsVip(true);
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(List.of(testCustomer));
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
        when(walletRepository.findByOwnerUserUserIdAndWalletTypeAndGroupIsNull(1L, WalletType.PERSONAL))
                .thenReturn(Optional.of(testWallet));
        when(bookingRepository.countByUser_UserId(1L)).thenReturn(0L);

        CustomerSpecification spec = CustomerSpecification.builder().isVip(true).build();
        Page<CustomerListItem> result = adminService.listCustomers(spec, pageable);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsVip());
    }

    // ── FR-002: 360° detail ─────────────────────────────────────────────────

    @Test
    void getCustomerDetail360_ReturnsAllSections() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(walletRepository.findByOwnerUserUserIdAndWalletTypeAndGroupIsNull(1L, WalletType.PERSONAL))
                .thenReturn(Optional.of(testWallet));
        when(bookingRepository.countByUser_UserId(1L)).thenReturn(10L);
        when(mealTicketRepository.countByUserUserIdAndStatus(1L, TicketStatus.UNUSED)).thenReturn(3L);
        when(userVoucherRepository.findByUserUserIdOrderByClaimedAtDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(customerNoteRepository.countPinnedByUserId(1L)).thenReturn(1L);
        when(loyaltyPointLedgerRepository.findFirstByUserUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(activityEventRepository.findByUserUserIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(activityEventRepository.getEventTypeCounts(1L)).thenReturn(new Object[]{});

        CustomerDetail360Response result = adminService.getCustomerDetail360(1L);

        assertEquals(1L, result.getUserId());
        assertEquals("customer@test.com", result.getEmail());
        assertEquals("GOLD", result.getCurrentTier());
        assertEquals(new BigDecimal("500.00"), result.getWalletBalance());
        assertEquals(10L, result.getTotalBookings());
        assertEquals(3L, result.getActiveMealTickets());
        assertEquals(1L, result.getPinnedNotes());
    }

    @Test
    void getCustomerDetail360_NotFound_ThrowsBusinessException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.getCustomerDetail360(999L));

        assertEquals("CUSTOMER_NOT_FOUND", ex.getErrorCode());
    }

    // ── FR-003: VIP marking ───────────────────────────────────────────────

    @Test
    void setVipStatus_MarkAsVip_UpdatesUserAndRecordsActivity() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VipStatusResponse result = adminService.setVipStatus(1L, 2L, true);

        assertTrue(result.getIsVip());
        assertNotNull(result.getVipMarkedAt());
        assertEquals(2L, result.getVipMarkedBy());
        verify(activityRecorder).recordVipMarked(1L, true, 2L);
    }

    @Test
    void setVipStatus_RemoveVip_ClearsVipFields() {
        testCustomer.setIsVip(true);
        testCustomer.setVipMarkedAt(LocalDateTime.now().minusDays(10));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        VipStatusResponse result = adminService.setVipStatus(1L, 2L, false);

        assertFalse(result.getIsVip());
        assertNull(result.getVipMarkedAt());
        verify(activityRecorder).recordVipMarked(1L, false, 2L);
    }

    // ── FR-004: Notes ─────────────────────────────────────────────────────

    @Test
    void addNote_ValidRequest_CreatesNote() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));
        CustomerNote savedNote = CustomerNote.builder()
                .noteId(1L).user(testCustomer).author(testAdmin)
                .content("Test note").isPinned(false)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .build();
        when(customerNoteRepository.save(any())).thenReturn(savedNote);

        CustomerNoteRequest request = new CustomerNoteRequest("Test note", false);
        CustomerNoteResponse result = adminService.addNote(1L, 2L, request);

        assertEquals("Test note", result.getContent());
        assertEquals(2L, result.getAuthorId());
        verify(customerNoteRepository).save(any());
    }

    @Test
    void addNote_EmptyContent_ThrowsBusinessException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));

        CustomerNoteRequest request = new CustomerNoteRequest("   ", false);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.addNote(1L, 2L, request));

        assertEquals("INVALID_NOTE_CONTENT", ex.getErrorCode());
    }

    @Test
    void deleteNote_NoteNotFound_ThrowsBusinessException() {
        when(customerNoteRepository.findByNoteIdAndUserUserId(999L, 1L))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.deleteNote(999L, 1L));

        assertEquals("NOTE_NOT_FOUND", ex.getErrorCode());
    }

    // ── FR-006: Activity timeline ───────────────────────────────────────────

    @Test
    void getActivityTimeline_ReturnsPaginatedEvents() {
        CustomerActivityEvent event = CustomerActivityEvent.builder()
                .eventId(1L).user(testCustomer).eventType("BOOKING_CREATED")
                .eventSummary("Booking #100 created").createdAt(LocalDateTime.now())
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(activityEventRepository.findByUserUserIdOrderByCreatedAtDesc(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(event)));

        Pageable pageable = PageRequest.of(0, 20);
        Page<CustomerActivityEventResponse> result = adminService.getActivityTimeline(1L, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("BOOKING_CREATED", result.getContent().get(0).getEventType());
    }

    @Test
    void getActivityTimeline_FilterByType_ReturnsFilteredEvents() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(activityEventRepository.findByUserUserIdAndEventTypeOrderByCreatedAtDesc(
                eq(1L), eq("PAYMENT_RECEIVED"), any()))
                .thenReturn(new PageImpl<>(List.of()));

        Pageable pageable = PageRequest.of(0, 20);
        adminService.getActivityTimeline(1L, "PAYMENT_RECEIVED", pageable);

        verify(activityEventRepository).findByUserUserIdAndEventTypeOrderByCreatedAtDesc(
                eq(1L), eq("PAYMENT_RECEIVED"), any());
    }

    // ── FR-007: Statistics ─────────────────────────────────────────────────

    @Test
    void getStats_ReturnsAggregateCounts() {
        User customer1 = User.builder().userId(1L).role("CUSTOMER").status("ACTIVE")
                .accountType("CUSTOMER").currentTier("GOLD").isVip(true)
                .createdAt(LocalDateTime.now().minusDays(5))
                .lastLoginAt(LocalDateTime.now().minusDays(2)).build();
        User customer2 = User.builder().userId(2L).role("CUSTOMER").status("ACTIVE")
                .accountType("CORPORATE_MEMBER").currentTier("SILVER").isVip(false)
                .createdAt(LocalDateTime.now().minusDays(40))
                .lastLoginAt(LocalDateTime.now().minusMonths(2)).build();
        User adminUser = User.builder().userId(3L).role("ADMIN").build();

        when(userRepository.findAll()).thenReturn(List.of(customer1, customer2, adminUser));

        CustomerStatsResponse result = adminService.getStats();

        assertEquals(2, result.getTotalCustomers());
        assertEquals(1, result.getActiveCustomers()); // only customer1 logged in 30 days
        assertEquals(1, result.getVipCount());
        assertEquals(1, result.getByTier().get("GOLD"));
        assertEquals(1, result.getByTier().get("SILVER"));
        assertEquals(1, result.getByAccountType().get("CUSTOMER"));
        assertEquals(1, result.getByAccountType().get("CORPORATE_MEMBER"));
    }

    // ── FR-005: Bulk actions ───────────────────────────────────────────────

    @Test
    void executeBulkAction_LockAccounts_UpdatesAllSelected() {
        User target1 = User.builder().userId(10L).status("ACTIVE").build();
        User target2 = User.builder().userId(11L).status("ACTIVE").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));
        when(userRepository.findById(10L)).thenReturn(Optional.of(target1));
        when(userRepository.findById(11L)).thenReturn(Optional.of(target2));
        when(bulkActionLogRepository.save(any())).thenAnswer(i -> {
            BulkActionLog log = i.getArgument(0);
            log.setBulkActionId(1L);
            return log;
        });

        BulkActionRequest request = new BulkActionRequest(
                List.of(10L, 11L), "LOCK_ACCOUNTS", null);

        BulkActionResponse result = adminService.executeBulkAction(2L, request);

        assertEquals(2, result.getAffectedCount());
        assertEquals("LOCK_ACCOUNTS", result.getActionType());
        verify(userRepository, times(2)).save(any());
    }

    @Test
    void executeBulkAction_EmptyCustomerList_ThrowsBusinessException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));

        BulkActionRequest request = new BulkActionRequest(List.of(), "LOCK_ACCOUNTS", null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.executeBulkAction(2L, request));

        assertEquals("NO_CUSTOMERS_SELECTED", ex.getErrorCode());
    }

    @Test
    void executeBulkAction_UnknownAction_ThrowsBusinessException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));

        BulkActionRequest request = new BulkActionRequest(List.of(1L), "UNKNOWN_ACTION", null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.executeBulkAction(2L, request));

        assertEquals("INVALID_BULK_ACTION", ex.getErrorCode());
    }

    @Test
    void executeBulkAction_ApplyTier_UpdatesTierAndRecordsActivity() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(testAdmin));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(bulkActionLogRepository.save(any())).thenAnswer(i -> {
            BulkActionLog log = i.getArgument(0);
            log.setBulkActionId(1L);
            return log;
        });

        BulkActionRequest request = new BulkActionRequest(
                List.of(1L), "APPLY_TIER", Map.of("tier", "PLATINUM"));

        BulkActionResponse result = adminService.executeBulkAction(2L, request);

        assertEquals(1, result.getAffectedCount());
        assertEquals("PLATINUM", testCustomer.getCurrentTier());
        verify(activityRecorder).recordTierChanged(eq(1L), eq("GOLD"), eq("PLATINUM"), eq(2L));
    }

    // ── FR-009: Group members ─────────────────────────────────────────────

    @Test
    void getGroupMembers_OwnerAccess_ReturnsMembers() {
        User owner = User.builder().userId(5L).role("CORPORATE_MEMBER").build();
        Group group = Group.builder().groupId(1L).ownerUser(owner).build();
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> members = new PageImpl<>(List.of(testCustomer));
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(members);

        Page<GroupMemberListItem> result = adminService.getGroupMembers(1L, 5L, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getGroupMembers_WrongOwner_ThrowsAccessDenied() {
        User wrongOwner = User.builder().userId(99L).role("CORPORATE_MEMBER").build();
        Group group = Group.builder().groupId(1L).ownerUser(wrongOwner).build();
        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.getGroupMembers(1L, 5L, null, PageRequest.of(0, 20)));

        assertEquals("ACCESS_DENIED", ex.getErrorCode());
    }
}
