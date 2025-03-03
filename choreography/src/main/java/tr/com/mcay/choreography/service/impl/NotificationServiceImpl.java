package tr.com.mcay.choreography.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.com.mcay.choreography.dto.NotificationRequest;
import tr.com.mcay.choreography.dto.NotificationResponse;
import tr.com.mcay.choreography.entity.Notification;
import tr.com.mcay.choreography.exception.ResourceNotFoundException;
import tr.com.mcay.choreography.repository.NotificationRepository;
import tr.com.mcay.choreography.service.NotificationService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse sendNotification(NotificationRequest request) {
        Notification notification = Notification.builder()
                .customerId(request.getCustomerId())
                .message(request.getMessage())
                .type(request.getType())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        return NotificationResponse.builder()
                .id(savedNotification.getId())
                .customerId(savedNotification.getCustomerId())
                .message(savedNotification.getMessage())
                .isRead(savedNotification.isRead())
                .type(savedNotification.getType())
                .createdAt(savedNotification.getCreatedAt())
                .build();
    }

    @Override
    public List<NotificationResponse> getCustomerNotifications(Long customerId) {
        List<Notification> notifications = notificationRepository.findByCustomerId(customerId);
        
        return notifications.stream()
                .map(notification -> NotificationResponse.builder()
                        .id(notification.getId())
                        .customerId(notification.getCustomerId())
                        .message(notification.getMessage())
                        .isRead(notification.isRead())
                        .type(notification.getType())
                        .createdAt(notification.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);

        return NotificationResponse.builder()
                .id(updatedNotification.getId())
                .customerId(updatedNotification.getCustomerId())
                .message(updatedNotification.getMessage())
                .isRead(updatedNotification.isRead())
                .type(updatedNotification.getType())
                .createdAt(updatedNotification.getCreatedAt())
                .build();
    }
} 