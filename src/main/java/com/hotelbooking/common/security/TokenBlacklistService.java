package com.hotelbooking.common.security;
import com.hotelbooking.auth.RevokedToken;
import com.hotelbooking.auth.RevokedTokenRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public void blacklistToken(String token, String tokenType, String email) {
        if (token == null || token.isEmpty()) {
            return;
        }

        try {
            if (revokedTokenRepository.existsByToken(token)) {
                log.info("Token is already blacklisted");
                return;
            }

            Date expirationDate = jwtService.extractAllClaims(token).getExpiration();
            LocalDateTime expiryTime = expirationDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            RevokedToken revokedToken = RevokedToken.builder()
                    .token(token)
                    .tokenType(tokenType)
                    .email(email)
                    .expiryTime(expiryTime)
                    .build();

            revokedTokenRepository.save(revokedToken);
            log.info("Successfully blacklisted {} token for user: {}", tokenType, email);
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage(), e);
        }
    }

    public boolean isTokenRevoked(String token) {
        if (token == null || token.isEmpty()) {
            return true;
        }
        return revokedTokenRepository.existsByToken(token);
    }
}
