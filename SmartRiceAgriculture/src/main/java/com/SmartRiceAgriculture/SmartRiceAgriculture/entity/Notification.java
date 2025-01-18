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

    public enum NotificationType {
        // Existing types
        BID_PLACED,
        BID_ACCEPTED,
        BID_REJECTED,
        BID_EXPIRED,
        ORDER_CREATED,
        ORDER_STATUS_CHANGE,
        PAYMENT_RECEIVED,
        PAYMENT_REMINDER,
        ADMIN_BROADCAST,

        // New fertilizer-related types
        FERTILIZER_ALLOCATED,
        FERTILIZER_READY,
        FERTILIZER_COLLECTED,
        FERTILIZER_EXPIRED
    }

    public enum Priority {
        MEDIUM
    }
}
