package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.AdminBroadcastRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.NotificationRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.NotificationRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.mapper.NotificationMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    public List<Notification> getUserNotifications(String userNic) {
        if (userNic == null) {
            logger.warn("User NIC is null, returning empty notification list");
            return Collections.emptyList();
        }
        return notificationRepository.findByRecipientNicOrRecipientNicIsNullOrderByCreatedDateDesc(userNic);
    }

    public Notification createNotification(String title, String description, String recipientNic,
                                           Notification.NotificationType type, Long bidId, Long orderId) {
        return createNotification(new NotificationRequestDTO(title, description, recipientNic, type, null, bidId, orderId));
    }

    public Notification createNotification(NotificationRequestDTO request) {
        if (request == null || request.getTitle() == null || request.getDescription() == null) {
            logger.error("Invalid notification request: title or description is null");
            throw new IllegalArgumentException("Title and description are required");
        }

        Notification notification = notificationMapper.toEntity(request);
        if (notification == null) {
            logger.error("Failed to map notification request to entity");
            throw new IllegalStateException("Failed to create notification entity");
        }

        try {
            logger.debug("Saving notification with title: {} and priority: {}", notification.getTitle(), notification.getPriority());
            return notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error saving notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save notification: " + e.getMessage(), e);
        }
    }

    public Notification createBroadcast(String title, String description) {
        return createBroadcast(new AdminBroadcastRequestDTO(title, description));
    }

    public Notification createBroadcast(AdminBroadcastRequestDTO request) {
        if (request == null || request.getTitle() == null || request.getDescription() == null) {
            logger.error("Invalid broadcast request: title or description is null");
            throw new IllegalArgumentException("Title and description are required");
        }

        NotificationRequestDTO dto = new NotificationRequestDTO();
        dto.setTitle(request.getTitle());
        dto.setDescription(request.getDescription());
        dto.setType(Notification.NotificationType.ADMIN_BROADCAST);

        Notification notification = notificationMapper.toEntity(dto);
        if (notification == null) {
            logger.error("Failed to map broadcast request to entity");
            throw new IllegalStateException("Failed to create broadcast entity");
        }

        try {
            logger.debug("Saving broadcast with title: {} and priority: {}", notification.getTitle(), notification.getPriority());
            return notificationRepository.save(notification);
        } catch (Exception e) {
            logger.error("Error saving broadcast: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save broadcast: " + e.getMessage(), e);
        }
    }

    public List<Notification> getAllBroadcasts() {
        try {
            List<Notification> broadcasts = notificationRepository.findByType(Notification.NotificationType.ADMIN_BROADCAST);
            if (broadcasts == null || broadcasts.isEmpty()) {
                logger.info("No broadcasts found");
            }
            return broadcasts;
        } catch (Exception e) {
            logger.error("Error fetching broadcasts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch broadcasts: " + e.getMessage(), e);
        }
    }

    public void deleteNotification(Long id) {
        try {
            notificationRepository.deleteById(id);
            logger.info("Notification with id {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Error deleting notification with id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete notification: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Notification markAsRead(Long id, String userNic) {
        if (id == null || userNic == null) {
            logger.error("Invalid input: id or userNic is null");
            throw new IllegalArgumentException("Invalid notification id or user nic");
        }

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found with id: " + id));

        if (notification.getRecipientNic() != null && !notification.getRecipientNic().equals(userNic)) {
            logger.warn("User {} does not have access to notification {}", userNic, id);
            throw new IllegalStateException("User does not have access to this notification");
        }

        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userNic) {
        if (userNic == null) {
            logger.error("User NIC is null");
            throw new IllegalArgumentException("User nic cannot be null");
        }

        List<Notification> notifications = notificationRepository
                .findByRecipientNicOrRecipientNicIsNullOrderByCreatedDateDesc(userNic);

        if (notifications.isEmpty()) {
            logger.info("No notifications found for user {}", userNic);
            return;
        }

        notifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(notifications);
        logger.info("All notifications marked as read for user {}", userNic);
    }

    public void createBidNotification(String recipientNic, Long bidId, Notification.NotificationType type, String bidAmount) {
        if (recipientNic == null || bidId == null || type == null) {
            logger.error("Invalid bid notification parameters: recipientNic, bidId, or type is null");
            throw new IllegalArgumentException("Required parameters cannot be null");
        }

        String title = getBidNotificationTitle(type);
        String description = getBidNotificationDescription(type, bidAmount);
        createNotification(title, description, recipientNic, type, bidId, null);
    }

    public void createOrderNotification(String recipientNic, Long orderId, String orderNumber, Notification.NotificationType type) {
        if (recipientNic == null || orderId == null || orderNumber == null || type == null) {
            logger.error("Invalid order notification parameters: one or more required fields are null");
            throw new IllegalArgumentException("Required parameters cannot be null");
        }

        String title = "Order Update: " + orderNumber;
        String description = getOrderNotificationDescription(type, orderNumber);
        createNotification(title, description, recipientNic, type, null, orderId);
    }

    public void createPaymentNotification(String recipientNic, Long orderId, String orderNumber, Notification.NotificationType type, Float amount) {
        if (recipientNic == null || orderId == null || type == null) {
            logger.error("Invalid payment notification parameters: one or more required fields are null");
            throw new IllegalArgumentException("Required parameters cannot be null");
        }

        String title = getPaymentNotificationTitle(type);
        String description = getPaymentNotificationDescription(type, amount, orderNumber);
        createNotification(title, description, recipientNic, type, null, orderId);
    }

    public void createFertilizerNotification(String recipientNic, Notification.NotificationType type, Float amount,
                                             String season, Integer year, String location, String referenceNumber) {
        if (recipientNic == null || type == null) {
            logger.error("Invalid fertilizer notification parameters: recipientNic or type is null");
            throw new IllegalArgumentException("Required parameters cannot be null");
        }

        String title = getFertilizerNotificationTitle(type);
        String description = getFertilizerNotificationDescription(type, amount, season, year, location, referenceNumber);
        createNotification(title, description, recipientNic, type, null, null);
    }

    public void createFertilizerBroadcast(String farmerNic, Float amount, String action, String referenceNumber) {
        if (farmerNic == null || amount == null || action == null) {
            logger.error("Invalid fertilizer broadcast parameters: one or more required fields are null");
            throw new IllegalArgumentException("Required parameters cannot be null");
        }

        String title = "Fertilizer Allocation Update";
        String description = String.format("Farmer %s has %s %.2fkg of fertilizer. Reference Number: %s", farmerNic, action, amount, referenceNumber);
        createBroadcast(title, description);
    }

    private static String getBidNotificationTitle(Notification.NotificationType type) {
        return switch (type) {
            case BID_PLACED -> "New Bid Received";
            case BID_ACCEPTED -> "Bid Accepted";
            case BID_REJECTED -> "Bid Rejected";
            case BID_EXPIRED -> "Bid Expired";
            default -> "Bid Update";
        };
    }

    private static String getBidNotificationDescription(Notification.NotificationType type, String bidAmount) {
        return switch (type) {
            case BID_PLACED -> "A new bid of Rs. " + bidAmount + " has been placed on your listing.";
            case BID_ACCEPTED -> "Your bid of Rs. " + bidAmount + " has been accepted.";
            case BID_REJECTED -> "Your bid of Rs. " + bidAmount + " was not accepted.";
            case BID_EXPIRED -> "Your bid listing has expired.";
            default -> "Your bid status has been updated.";
        };
    }

    private static String getOrderNotificationDescription(Notification.NotificationType type, String orderNumber) {
        return switch (type) {
            case ORDER_CREATED -> "New order " + orderNumber + " has been created.";
            case ORDER_STATUS_CHANGE -> "Order " + orderNumber + " status has been updated.";
            default -> "Order " + orderNumber + " has been updated.";
        };
    }

    private static String getPaymentNotificationTitle(Notification.NotificationType type) {
        return switch (type) {
            case PAYMENT_RECEIVED -> "Payment Received";
            case PAYMENT_REMINDER -> "Payment Reminder";
            default -> "Payment Update";
        };
    }

    private static String getPaymentNotificationDescription(Notification.NotificationType type, Float amount, String orderNumber) {
        return switch (type) {
            case PAYMENT_RECEIVED -> String.format("Payment of Rs. %.2f received for order %s", amount, orderNumber);
            case PAYMENT_REMINDER -> "Payment deadline approaching for order " + orderNumber;
            default -> "Payment status updated for order " + orderNumber;
        };
    }

    private static String getFertilizerNotificationTitle(Notification.NotificationType type) {
        return switch (type) {
            case FERTILIZER_ALLOCATED -> "New Fertilizer Allocation";
            case FERTILIZER_READY -> "Fertilizer Ready for Collection";
            case FERTILIZER_COLLECTED -> "Fertilizer Collection Confirmed";
            case FERTILIZER_EXPIRED -> "Fertilizer Allocation Expired";
            default -> "Fertilizer Update";
        };
    }

    private static String getFertilizerNotificationDescription(Notification.NotificationType type, Float amount, String season,
                                                               Integer year, String location, String referenceNumber) {
        return switch (type) {
            case FERTILIZER_ALLOCATED -> String.format("You have been allocated %.2fkg of fertilizer for %s season %d", amount, season, year);
            case FERTILIZER_READY -> String.format("Your fertilizer allocation (%.2fkg) is ready for collection at %s. Reference Number: %s", amount, location, referenceNumber);
            case FERTILIZER_COLLECTED -> String.format("Confirmed collection of %.2fkg fertilizer. Reference Number: %s", amount, referenceNumber);
            case FERTILIZER_EXPIRED -> String.format("Your fertilizer allocation of %.2fkg for %s season %d has expired", amount, season, year);
            default -> "Your fertilizer allocation status has been updated.";
        };
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldNotifications() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        try {
            notificationRepository.deleteByCreatedDateBefore(oneMonthAgo);
            logger.info("Deleted notifications older than {}", oneMonthAgo);
        } catch (Exception e) {
            logger.error("Error deleting old notifications: {}", e.getMessage(), e);
        }
    }
}