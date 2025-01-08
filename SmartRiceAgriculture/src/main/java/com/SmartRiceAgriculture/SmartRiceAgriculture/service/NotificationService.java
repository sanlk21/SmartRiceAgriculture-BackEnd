package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // Create individual notification
    public Notification createNotification(String title, String description,
                                           String recipientNic, Notification.NotificationType type,
                                           Long bidId, Long orderId) {

        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setDescription(description);
        notification.setRecipientNic(recipientNic);
        notification.setType(type);
        notification.setBidId(bidId);
        notification.setOrderId(orderId);

        return notificationRepository.save(notification);
    }

    // Create admin broadcast
    public Notification createBroadcast(String title, String description) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setDescription(description);
        notification.setType(Notification.NotificationType.ADMIN_BROADCAST);

        return notificationRepository.save(notification);
    }

    // Get user's notifications (individual + broadcasts)
    public List<Notification> getUserNotifications(String userNic) {
        return notificationRepository.findByRecipientNicOrRecipientNicIsNullOrderByCreatedDateDesc(
                userNic);
    }

    // Get all broadcasts
    public List<Notification> getAllBroadcasts() {
        return notificationRepository.findByType(
                Notification.NotificationType.ADMIN_BROADCAST);
    }

    // Delete a specific notification
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }

    // Create bid-related notifications
    public void createBidNotification(String recipientNic, Long bidId,
                                      Notification.NotificationType type, String bidAmount) {
        String title = switch (type) {
            case BID_PLACED -> "New Bid Received";
            case BID_ACCEPTED -> "Bid Accepted";
            case BID_REJECTED -> "Bid Rejected";
            case BID_EXPIRED -> "Bid Expired";
            default -> "Bid Update";
        };

        String description = switch (type) {
            case BID_PLACED -> "A new bid of Rs. " + bidAmount + " has been placed on your listing.";
            case BID_ACCEPTED -> "Your bid of Rs. " + bidAmount + " has been accepted.";
            case BID_REJECTED -> "Your bid of Rs. " + bidAmount + " was not accepted.";
            case BID_EXPIRED -> "Your bid listing has expired.";
            default -> "Your bid status has been updated.";
        };

        createNotification(title, description, recipientNic, type, bidId, null);
    }

    // Create order-related notifications
    public void createOrderNotification(String recipientNic, Long orderId,
                                        String orderNumber, Notification.NotificationType type) {
        String title = "Order Update: " + orderNumber;
        String description = switch (type) {
            case ORDER_CREATED -> "New order " + orderNumber + " has been created.";
            case ORDER_STATUS_CHANGE -> "Order " + orderNumber + " status has been updated.";
            default -> "Order " + orderNumber + " has been updated.";
        };

        createNotification(title, description, recipientNic, type, null, orderId);
    }

    // Create payment-related notifications
    public void createPaymentNotification(String recipientNic, Long orderId,
                                          String orderNumber, Notification.NotificationType type, Float amount) {
        String title = switch (type) {
            case PAYMENT_RECEIVED -> "Payment Received";
            case PAYMENT_REMINDER -> "Payment Reminder";
            default -> "Payment Update";
        };

        String description = switch (type) {
            case PAYMENT_RECEIVED ->
                    "Payment of Rs. " + amount + " received for order " + orderNumber;
            case PAYMENT_REMINDER ->
                    "Payment deadline approaching for order " + orderNumber;
            default -> "Payment status updated for order " + orderNumber;
        };

        createNotification(title, description, recipientNic, type, null, orderId);
    }

    // Scheduled task to delete old notifications (runs daily at midnight)
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldNotifications() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        notificationRepository.deleteByCreatedDateBefore(oneMonthAgo);
    }
}