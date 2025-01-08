package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.NotificationType;
import lombok.Data;

@Data
public class NotificationRequestDTO {
    private String title;
    private String description;
    private String recipientNic;
    private NotificationType type;
    private Long bidId;
    private Long orderId;
}
