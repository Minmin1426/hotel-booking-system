package com.hotelbooking.admin;

import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerActivityRecorderTest {

    @Mock private CustomerActivityEventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private ObjectMapper objectMapper;
    @InjectMocks
    private CustomerActivityRecorder recorder;

    private User testUser;
    private User actorUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().userId(1L).email("test@hotel.com").fullName("Test User").build();
        actorUser = User.builder().userId(2L).email("admin@hotel.com").fullName("Admin").build();
    }

    @Test
    void record_ValidUser_SavesEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findById(2L)).thenReturn(Optional.of(actorUser));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (JsonProcessingException e) {}
        doAnswer(inv -> { latch.countDown(); return null; }).when(eventRepository).save(any());

        recorder.record(1L, "BOOKING_CREATED", "Booking #100 created", Map.of("bookingId", 100), 2L);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Async method did not complete in time");
        ArgumentCaptor<CustomerActivityEvent> captor = ArgumentCaptor.forClass(CustomerActivityEvent.class);
        verify(eventRepository).save(captor.capture());

        CustomerActivityEvent saved = captor.getValue();
        assertEquals("BOOKING_CREATED", saved.getEventType());
        assertEquals("Booking #100 created", saved.getEventSummary());
        assertNotNull(saved.getEventMetadata());
        assertEquals(testUser, saved.getUser());
        assertEquals(actorUser, saved.getActor());
    }

    @Test
    void record_NonExistentUser_SkipsSave() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        recorder.record(999L, "TEST_EVENT", "Should not save", null, null);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void recordBookingCreated_ConvenienceMethod_SavesCorrectEvent() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (JsonProcessingException e) {}

        recorder.recordBookingCreated(1L, 100L, "Grand Hotel", 2L);

        ArgumentCaptor<CustomerActivityEvent> captor = ArgumentCaptor.forClass(CustomerActivityEvent.class);
        verify(eventRepository, timeout(2000)).save(captor.capture());

        CustomerActivityEvent saved = captor.getValue();
        assertEquals("BOOKING_CREATED", saved.getEventType());
        assertTrue(saved.getEventSummary().contains("100"));
        assertTrue(saved.getEventSummary().contains("Grand Hotel"));
        assertTrue(saved.getEventMetadata() != null);
    }

    @Test
    void recordVoucherRedeemed_ConvenienceMethod_SavesCorrectEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (JsonProcessingException e) {}
        doAnswer(inv -> { latch.countDown(); return null; }).when(eventRepository).save(any());

        recorder.recordVoucherRedeemed(1L, "SUMMER20", 500L);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Async method did not complete in time");
        ArgumentCaptor<CustomerActivityEvent> captor = ArgumentCaptor.forClass(CustomerActivityEvent.class);
        verify(eventRepository).save(captor.capture());

        CustomerActivityEvent saved = captor.getValue();
        assertEquals("VOUCHER_REDEEMED", saved.getEventType());
        assertTrue(saved.getEventSummary().contains("SUMMER20"));
    }

    @Test
    void recordTierChanged_RecordsOldAndNewTier() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (JsonProcessingException e) {}
        doAnswer(inv -> { latch.countDown(); return null; }).when(eventRepository).save(any());

        recorder.recordTierChanged(1L, "GOLD", "PLATINUM", 2L);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Async method did not complete in time");
        ArgumentCaptor<CustomerActivityEvent> captor = ArgumentCaptor.forClass(CustomerActivityEvent.class);
        verify(eventRepository).save(captor.capture());

        CustomerActivityEvent saved = captor.getValue();
        assertEquals("TIER_CHANGED", saved.getEventType());
        assertTrue(saved.getEventSummary().contains("GOLD"));
        assertTrue(saved.getEventSummary().contains("PLATINUM"));
    }

    @Test
    void recordVipMarked_SetTrue_RecordsMarkedEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (JsonProcessingException e) {}
        doAnswer(inv -> { latch.countDown(); return null; }).when(eventRepository).save(any());

        recorder.recordVipMarked(1L, true, 2L);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Async method did not complete in time");
        ArgumentCaptor<CustomerActivityEvent> captor = ArgumentCaptor.forClass(CustomerActivityEvent.class);
        verify(eventRepository).save(captor.capture());

        assertEquals("VIP_MARKED", captor.getValue().getEventType());
    }

    @Test
    void recordVipMarked_RemoveVip_RecordsRemovedEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        try { when(objectMapper.writeValueAsString(any())).thenReturn("{}"); } catch (JsonProcessingException e) {}
        doAnswer(inv -> { latch.countDown(); return null; }).when(eventRepository).save(any());

        recorder.recordVipMarked(1L, false, 2L);

        assertTrue(latch.await(2, TimeUnit.SECONDS), "Async method did not complete in time");
        ArgumentCaptor<CustomerActivityEvent> captor = ArgumentCaptor.forClass(CustomerActivityEvent.class);
        verify(eventRepository).save(captor.capture());

        assertEquals("VIP_REMOVED", captor.getValue().getEventType());
    }
}
