package com.hotelbooking.loyalty;

import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Daily scheduler to re-evaluate tier eligibility for all active users.
 * Handles demotion when rolling 12-month spend drops below the current tier threshold.
 * Runs at 2 AM daily.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TierEvaluationScheduler {

    private final UserRepository userRepository;
    private final LoyaltyService loyaltyService;

    /**
     * Daily demotion check — runs at 2 AM every day.
     * Evaluates all users with an existing tier (non-default).
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyDemotionCheck() {
        log.info("TierScheduler: Starting daily tier evaluation");

        List<User> allUsers = userRepository.findAll();
        int evaluated = 0;
        int demoted = 0;

        for (User user : allUsers) {
            if (user.getCurrentTier() == null) continue;
            evaluated++;
            try {
                loyaltyService.evaluateTier(user.getUserId());
            } catch (Exception e) {
                log.error("TierScheduler: Failed to evaluate tier for user {}: {}",
                        user.getUserId(), e.getMessage());
            }
        }

        log.info("TierScheduler: Daily evaluation complete. Evaluated={}, TotalUsers={}",
                evaluated, allUsers.size());
    }
}
