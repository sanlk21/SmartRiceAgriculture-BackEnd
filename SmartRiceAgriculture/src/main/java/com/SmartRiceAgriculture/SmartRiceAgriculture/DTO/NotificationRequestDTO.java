package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.NotificationType;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private String title;
    private String description;
    private String recipientNic;
    private NotificationType type;
    private Priority priority = Priority.MEDIUM;
    private Long bidId;
    private Long orderId;
}