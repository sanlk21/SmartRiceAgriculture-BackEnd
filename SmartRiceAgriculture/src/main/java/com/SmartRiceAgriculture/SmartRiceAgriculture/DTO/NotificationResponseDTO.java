package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.NotificationType;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification.Priority;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String recipientNic;
    private NotificationType type;
    private Priority priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;

    private Long bidId;
    private Long orderId;
    private boolean isRead;

    // Additional fields for UI display
    public String getTimeAgo() {
        // Add logic to calculate time ago string
        return calculateTimeAgo(createdDate);
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(dateTime, now).toMinutes();

        if (minutes < 1) return "just now";
        if (minutes < 60) return minutes + " minutes ago";

        long hours = minutes / 60;
        if (hours < 24) return hours + " hours ago";

        long days = hours / 24;
        if (days < 30) return days + " days ago";

        long months = days / 30;
        if (months < 12) return months + " months ago";

        return days / 365 + " years ago";
    }
}