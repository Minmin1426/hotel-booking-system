package com.hotelbooking.auth;
import com.hotelbooking.auth.dto.ForgotPasswordRequest;
import com.hotelbooking.auth.dto.ResetPasswordRequest;
import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.user.User;
import com.hotelbooking.user.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PasswordServiceImpl implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("UC32: Requesting password reset for email: {}", request.getEmail());

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    String token = UUID.randomUUID().toString();

                    PasswordResetToken resetToken = PasswordResetToken.builder()
                            .token(token)
                            .user(user)
                            .used(false)
                            .expiryTime(LocalDateTime.now().plusMinutes(5)) // Expires in 5 minutes as per Business Rules
                            .build();

                    tokenRepository.save(resetToken);

                    emailService.sendResetPasswordEmail(
                            user.getEmail(),
                            token
                    );
                });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        log.info("UC4: Resetting password using token");

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }

        PasswordResetToken token = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired token"));

        if (token.isUsed() || token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        log.info("UC4: Password reset successfully for user: {}", user.getEmail());
    }
}
