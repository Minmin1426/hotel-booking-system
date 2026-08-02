package com.hotelbooking.notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

import com.hotelbooking.common.utils.EmailService;
import com.hotelbooking.notification.dto.NotificationRequest;
import com.hotelbooking.notification.dto.NotificationResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final List<NotificationResponse> inbox = new CopyOnWriteArrayList<>();

    @Override
    public NotificationResponse broadcast(NotificationRequest request) {
        NotificationResponse response = NotificationResponse.builder()
                .id(System.currentTimeMillis())
                .title(request.getTitle())
                .message(request.getMessage())
                .channel(request.getChannel())
                .createdAt(LocalDateTime.now())
                .build();

        inbox.add(0, response);

        if ("EMAIL".equalsIgnoreCase(request.getChannel()) || "BOTH".equalsIgnoreCase(request.getChannel())) {
            emailService.sendBookingConfirmationEmail("guest@example.com", request.getTitle());
        }

        log.info("Notification broadcasted via {}", request.getChannel());
        return response;
    }

    @Override
    public List<NotificationResponse> listRecent() {
        return inbox.stream().limit(20).toList();
    }
}
