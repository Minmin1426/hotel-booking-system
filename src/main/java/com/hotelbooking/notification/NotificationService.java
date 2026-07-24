package com.hotelbooking.notification;

import java.util.List;

import com.hotelbooking.notification.dto.NotificationRequest;
import com.hotelbooking.notification.dto.NotificationResponse;

public interface NotificationService {
    NotificationResponse broadcast(NotificationRequest request);
    List<NotificationResponse> listRecent();
}
