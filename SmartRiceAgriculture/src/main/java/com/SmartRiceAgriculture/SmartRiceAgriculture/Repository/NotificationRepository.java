package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Find user's notifications (including broadcasts where recipientNic is null)
    List<Notification> findByRecipientNicOrRecipientNicIsNullOrderByCreatedDateDesc(String recipientNic);

    // Find all broadcasts
    List<Notification> findByType(Notification.NotificationType type);

    // Delete notifications older than 1 month
    void deleteByCreatedDateBefore(LocalDateTime date);

    // Find unread notifications for a user
    List<Notification> findByRecipientNicAndIsReadFalse(String recipientNic);

    // Count unread notifications for a user
    long countByRecipientNicAndIsReadFalse(String recipientNic);

    // Mark all notifications as read for a user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientNic = ?1 OR n.recipientNic IS NULL")
    void markAllAsRead(String recipientNic);

    // Find notifications by type and recipient
    List<Notification> findByTypeAndRecipientNicOrderByCreatedDateDesc(Notification.NotificationType type, String recipientNic);
}