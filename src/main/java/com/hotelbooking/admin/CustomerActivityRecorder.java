package com.hotelbooking.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

/**
 * Utility service for recording customer activity events from across the application.
 * Modules like Booking, Payment, Wallet, Voucher, Loyalty, MealTicket call this
 * to emit events for the admin activity timeline.
 *
 * Uses REQUIRES_NEW propagation so events are saved even if the caller's transaction rolls back.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerActivityRecorder {

    private final CustomerActivityEventRepository eventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String eventType, String summary, Map<String, Object> metadata, Long actorUserId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot record activity for non-existent user: {}", userId);
                return;
            }

            User actor = actorUserId != null
                    ? userRepository.findById(actorUserId).orElse(null)
                    : null;

            String metadataJson = null;
            if (metadata != null && !metadata.isEmpty()) {
                try {
                    metadataJson = objectMapper.writeValueAsString(metadata);
                } catch (JsonProcessingException e) {
                    log.warn("Failed to serialize metadata for event type {}: {}", eventType, e.getMessage());
                }
            }

            CustomerActivityEvent event = CustomerActivityEvent.builder()
                    .user(user)
                    .eventType(eventType)
                    .eventSummary(summary)
                    .eventMetadata(metadataJson)
                    .actor(actor)
                    .build();

            eventRepository.save(event);
            log.debug("Recorded activity event: {} for user {}", eventType, userId);
        } catch (Exception e) {
            log.error("Failed to record activity event {} for user {}: {}", eventType, userId, e.getMessage());
        }
    }

    // Convenience overloads
    public void record(Long userId, String eventType, String summary, Long actorUserId) {
        record(userId, eventType, summary, null, actorUserId);
    }

    public void record(Long userId, String eventType, String summary) {
        record(userId, eventType, summary, null, null);
    }

    // Convenience method for bookings
    public void recordBookingCreated(Long userId, Long bookingId, String hotelName, Long actorUserId) {
        record(userId, "BOOKING_CREATED",
                "Booking #" + bookingId + " created at " + hotelName,
                Map.of("bookingId", bookingId, "hotelName", hotelName),
                actorUserId);
    }

    public void recordBookingCancelled(Long userId, Long bookingId, String reason, Long actorUserId) {
        record(userId, "BOOKING_CANCELLED",
                "Booking #" + bookingId + " cancelled" + (reason != null ? ": " + reason : ""),
                Map.of("bookingId", bookingId, "reason", reason != null ? reason : ""),
                actorUserId);
    }

    public void recordPaymentReceived(Long userId, Long bookingId, String amount, Long actorUserId) {
        record(userId, "PAYMENT_RECEIVED",
                "Payment of " + amount + " received for booking #" + bookingId,
                Map.of("bookingId", bookingId, "amount", amount),
                actorUserId);
    }

    public void recordTierChanged(Long userId, String oldTier, String newTier, Long actorUserId) {
        record(userId, "TIER_CHANGED",
                "Tier changed from " + oldTier + " to " + newTier,
                Map.of("oldTier", oldTier, "newTier", newTier),
                actorUserId);
    }

    public void recordVoucherClaimed(Long userId, String voucherCode) {
        record(userId, "VOUCHER_CLAIMED",
                "Voucher " + voucherCode + " claimed",
                Map.of("voucherCode", voucherCode),
                null);
    }

    public void recordVoucherRedeemed(Long userId, String voucherCode, Long bookingId) {
        record(userId, "VOUCHER_REDEEMED",
                "Voucher " + voucherCode + " redeemed on booking #" + bookingId,
                Map.of("voucherCode", voucherCode, "bookingId", bookingId),
                null);
    }

    public void recordMealTicketScanned(Long userId, String ticketType) {
        record(userId, "MEAL_TICKET_SCANNED",
                "Meal ticket (" + ticketType + ") scanned",
                Map.of("ticketType", ticketType),
                null);
    }

    public void recordWalletDeposit(Long userId, String amount, Long actorUserId) {
        record(userId, "WALLET_DEPOSIT",
                "Wallet deposit of " + amount,
                Map.of("amount", amount),
                actorUserId);
    }

    public void recordVipMarked(Long userId, boolean isVip, Long adminId) {
        record(userId, isVip ? "VIP_MARKED" : "VIP_REMOVED",
                isVip ? "Customer marked as VIP" : "VIP status removed",
                Map.of("isVip", isVip),
                adminId);
    }
}
