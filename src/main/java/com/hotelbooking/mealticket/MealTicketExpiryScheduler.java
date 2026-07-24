package com.hotelbooking.mealticket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Daily job to mark UNUSED meal tickets past their expiry time as EXPIRED.
 * Runs at 1 AM every day.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MealTicketExpiryScheduler {

    private final MealTicketService mealTicketService;

    @Scheduled(cron = "0 0 1 * * *")
    public void expireOldTickets() {
        log.info("MealTicketExpiryScheduler: Starting daily expiry job");
        mealTicketService.expireOldTickets();
        log.info("MealTicketExpiryScheduler: Daily expiry job complete");
    }
}
