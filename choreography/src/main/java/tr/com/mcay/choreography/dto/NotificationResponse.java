package tr.com.mcay.choreography.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.mcay.choreography.entity.NotificationType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long customerId;
    private String message;
    private boolean isRead;
    private NotificationType type;
    private LocalDateTime createdAt;
} 