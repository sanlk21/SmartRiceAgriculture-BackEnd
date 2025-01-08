package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.NotificationType;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.Priority;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String recipientNic;
    private NotificationType type;
    private Priority priority;
    private LocalDateTime createdDate;
    private Long bidId;
    private Long orderId;
}
