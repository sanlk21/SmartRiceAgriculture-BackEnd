package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;


import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
