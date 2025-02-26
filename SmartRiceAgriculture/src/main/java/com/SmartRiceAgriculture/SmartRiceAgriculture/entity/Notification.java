package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdDate;

    // Recipient user's NIC (null for broadcast notifications)
    private String recipientNic;

    // Reference IDs for related entities
    private Long bidId;
    private Long orderId;

    @Column(nullable = false)
    private boolean isRead = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority = Priority.MEDIUM;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public enum NotificationType {
        // Bid-related notifications
        BID_PLACED,
        BID_ACCEPTED,
        BID_REJECTED,
        BID_EXPIRED,

        // Order-related notifications
        ORDER_CREATED,
        ORDER_STATUS_CHANGE,

        // Payment-related notifications
        PAYMENT_RECEIVED,
        PAYMENT_REMINDER,
        PAYMENT_COMPLETED,

        // Admin notifications
        ADMIN_BROADCAST,

        // Fertilizer-related notifications
        FERTILIZER_ALLOCATED,
        FERTILIZER_READY,
        FERTILIZER_COLLECTED,
        FERTILIZER_EXPIRED
    }

    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
}