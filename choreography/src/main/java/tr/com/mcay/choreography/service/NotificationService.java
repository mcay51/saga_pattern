package tr.com.mcay.choreography.service;

import tr.com.mcay.choreography.dto.NotificationRequest;
import tr.com.mcay.choreography.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse sendNotification(NotificationRequest request);
    List<NotificationResponse> getCustomerNotifications(Long customerId);
    NotificationResponse markAsRead(Long notificationId);
} 