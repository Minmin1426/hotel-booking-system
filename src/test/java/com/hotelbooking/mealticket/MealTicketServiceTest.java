package com.hotelbooking.mealticket;

import com.hotelbooking.booking.BookingRepository;
import com.hotelbooking.admin.CustomerActivityRecorder;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import com.hotelbooking.mealticket.dto.*;
import com.hotelbooking.mealticket.exception.*;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.hotelbooking.wallet.GroupMembership;
import com.hotelbooking.wallet.GroupMembershipRepository;
import com.hotelbooking.wallet.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MealTicketServiceTest {

    @Mock private MealTicketRepository ticketRepository;
    @Mock private MealTicketTypeRepository typeRepository;
    @Mock private MealTicketAuditLogRepository auditRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private GroupRepository groupRepository;
    @Mock private GroupMembershipRepository membershipRepository;
    @Mock private QrCodeGenerator qrCodeGenerator;
    @Mock private CustomerActivityRecorder activityRecorder;

    @InjectMocks
    private MealTicketServiceImpl mealTicketService;

    private User guest;
    private User staff;
    private User issuer;
    private MealTicketType breakfastType;
    private MealTicket unusedTicket;

    @BeforeEach
    void setUp() {
        guest = User.builder().userId(1L).fullName("Guest User").email("guest@test.com").build();
        staff = User.builder().userId(2L).fullName("Staff Member").email("staff@test.com").build();
        issuer = User.builder().userId(3L).fullName("Receptionist").email("reception@test.com").build();

        breakfastType = MealTicketType.builder()
                .typeId(1L).code("BREAKFAST_BUFFET").name("Breakfast Buffet")
                .defaultValidDays(1).defaultPrice(new BigDecimal("350000")).isActive(true)
                .build();

        unusedTicket = MealTicket.builder()
                .ticketId(100L).user(guest).ticketType(breakfastType)
                .qrCode("100:1:123456:nonce:sig").qrSignature("sig")
                .status(TicketStatus.UNUSED)
                .issuedAt(java.time.LocalDateTime.now().minusHours(1))
                .expiresAt(java.time.LocalDateTime.now().plusDays(1))
                .build();
    }

    // ── AC-061: Issue ticket ───────────────────────────────────────────────────

    @Test
    void issueTicket_NewTicket_GeneratesQrAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(guest));
        when(typeRepository.findByCodeAndIsActiveTrue("BREAKFAST_BUFFET"))
                .thenReturn(Optional.of(breakfastType));
        when(qrCodeGenerator.generateQr(anyLong(), eq(1L)))
                .thenReturn(new QrCodeGenerator.QrResult("100:1:123456:nonce:sig", "base64png", "sig"));
        when(ticketRepository.save(any(MealTicket.class))).thenAnswer(inv -> {
            MealTicket t = inv.getArgument(0);
            t.setTicketId(100L);
            return t;
        });
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MealTicketResponse response = mealTicketService.issueTicket(1L, null, "BREAKFAST_BUFFET", 1, 3L);

        assertEquals(100L, response.getTicketId());
        assertEquals("BREAKFAST_BUFFET", response.getTicketType());
        assertEquals("UNUSED", response.getStatus());
        assertEquals("Guest User", response.getUserFullName());
        verify(ticketRepository, times(1)).save(any(MealTicket.class));
        verify(auditRepository).save(any(MealTicketAuditLog.class));
    }

    @Test
    void issueTicket_InvalidType_ThrowsResourceNotFoundException() {
        when(typeRepository.findByCodeAndIsActiveTrue("INVALID_TYPE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                mealTicketService.issueTicket(1L, null, "INVALID_TYPE", 1, 3L));
    }

    // ── AC-063/064: Scan and consume ──────────────────────────────────────────

    @Test
    void scanAndConsume_ValidQr_UnusedTicket_MarksUsed() {
        when(qrCodeGenerator.verifyQr("100:1:123456:nonce:sig"))
                .thenReturn(new QrCodeGenerator.QrPayload(100L, 1L));
        when(ticketRepository.findByQrCode("100:1:123456:nonce:sig"))
                .thenReturn(Optional.of(unusedTicket));
        when(userRepository.findById(2L)).thenReturn(Optional.of(staff));
        when(ticketRepository.save(any(MealTicket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScanTicketResponse response = mealTicketService.scanAndConsume("100:1:123456:nonce:sig", 2L);

        assertEquals(100L, response.getTicketId());
        assertEquals("USED", response.getStatus());
        assertEquals("Guest User", response.getUserFullName());
        assertNotNull(response.getConsumedAt());
    }

    @Test
    void scanAndConsume_InvalidSignature_ThrowsInvalidQrCode() {
        when(qrCodeGenerator.verifyQr("tampered")).thenThrow(new SecurityException("INVALID_QR_CODE"));

        assertThrows(InvalidQrCodeException.class, () ->
                mealTicketService.scanAndConsume("tampered", 2L));
    }

    @Test
    void scanAndConsume_AlreadyUsed_ThrowsTicketAlreadyUsed() {
        unusedTicket.setStatus(TicketStatus.USED);
        when(qrCodeGenerator.verifyQr(anyString()))
                .thenReturn(new QrCodeGenerator.QrPayload(100L, 1L));
        when(ticketRepository.findByQrCode(anyString()))
                .thenReturn(Optional.of(unusedTicket));

        assertThrows(TicketAlreadyUsedException.class, () ->
                mealTicketService.scanAndConsume("100:1:123456:nonce:sig", 2L));
    }

    @Test
    void scanAndConsume_Expired_ThrowsTicketExpired() {
        unusedTicket.setStatus(TicketStatus.EXPIRED);
        when(qrCodeGenerator.verifyQr(anyString()))
                .thenReturn(new QrCodeGenerator.QrPayload(100L, 1L));
        when(ticketRepository.findByQrCode(anyString()))
                .thenReturn(Optional.of(unusedTicket));

        assertThrows(TicketExpiredException.class, () ->
                mealTicketService.scanAndConsume("100:1:123456:nonce:sig", 2L));
    }

    @Test
    void scanAndConsume_TicketNotFound_ThrowsTicketNotFound() {
        when(qrCodeGenerator.verifyQr(anyString()))
                .thenReturn(new QrCodeGenerator.QrPayload(999L, 1L));
        when(ticketRepository.findByQrCode(anyString())).thenReturn(Optional.empty());

        assertThrows(TicketNotFoundException.class, () ->
                mealTicketService.scanAndConsume("999:1:123456:nonce:sig", 2L));
    }

    @Test
    void scanAndConsume_WrongOwner_ThrowsInvalidQrCode() {
        when(qrCodeGenerator.verifyQr(anyString()))
                .thenReturn(new QrCodeGenerator.QrPayload(100L, 99L)); // wrong user
        when(ticketRepository.findByQrCode(anyString()))
                .thenReturn(Optional.of(unusedTicket));

        assertThrows(InvalidQrCodeException.class, () ->
                mealTicketService.scanAndConsume("100:1:123456:nonce:sig", 2L));
    }

    // ── AC-065: Bulk issue ─────────────────────────────────────────────────────

    @Test
    void issueBulkTickets_GroupMembers_CreatesTicketsForAll() {
        User member1 = User.builder().userId(10L).fullName("Member 1").build();
        User member2 = User.builder().userId(11L).fullName("Member 2").build();

        when(userRepository.findById(3L)).thenReturn(Optional.of(issuer));
        when(typeRepository.findByCodeAndIsActiveTrue("BREAKFAST_BUFFET"))
                .thenReturn(Optional.of(breakfastType));
        when(userRepository.findById(10L)).thenReturn(Optional.of(member1));
        when(userRepository.findById(11L)).thenReturn(Optional.of(member2));
        when(membershipRepository.findByGroupIdAndMemberUserId(1L, 10L))
                .thenReturn(Optional.of(GroupMembership.builder().build()));
        when(membershipRepository.findByGroupIdAndMemberUserId(1L, 11L))
                .thenReturn(Optional.of(GroupMembership.builder().build()));
        when(qrCodeGenerator.generateQr(anyLong(), anyLong()))
                .thenReturn(new QrCodeGenerator.QrResult("x:x:x:x:sig", "base64", "sig"));
        when(ticketRepository.save(any(MealTicket.class))).thenAnswer(inv -> {
            MealTicket t = inv.getArgument(0);
            t.setTicketId((long) (Math.random() * 1000));
            return t;
        });
        when(auditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        mealTicketService.issueBulkTickets(1L, "BREAKFAST_BUFFET", 1, 3L, List.of(10L, 11L));

        verify(ticketRepository, times(2)).save(any(MealTicket.class)); // 2 tickets × 1 save each
    }

    // ── Expiry job ────────────────────────────────────────────────────────────

    @Test
    void expireOldTickets_CallsRepository() {
        when(ticketRepository.expireOldTickets(any())).thenReturn(5);

        mealTicketService.expireOldTickets();

        verify(ticketRepository).expireOldTickets(any());
    }

    // ── QR image ──────────────────────────────────────────────────────────────

    @Test
    void getQrImage_WrongOwner_ThrowsTicketNotFound() {
        unusedTicket.getUser().setUserId(99L);
        when(ticketRepository.findById(100L)).thenReturn(Optional.of(unusedTicket));

        assertThrows(TicketNotFoundException.class, () ->
                mealTicketService.getQrImage(1L, 100L));
    }
}
