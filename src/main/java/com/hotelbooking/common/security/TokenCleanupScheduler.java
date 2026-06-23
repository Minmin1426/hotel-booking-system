package com.hotelbooking.common.security;
import com.hotelbooking.auth.RevokedTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupScheduler {

    private final RevokedTokenRepository revokedTokenRepository;

    // Run every hour to remove expired blacklisted tokens
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired blacklisted tokens...");
        try {
            int deletedCount = revokedTokenRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
            log.info("Completed blacklisted token cleanup. Removed {} expired tokens.", deletedCount);
        } catch (Exception e) {
            log.error("Failed to cleanup expired blacklisted tokens: {}", e.getMessage(), e);
        }
    }
}
