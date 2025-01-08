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
        BID_PLACED,           // New bid on farmer's listing
        BID_ACCEPTED,         // Buyer's bid was accepted
        BID_REJECTED,         // Buyer's bid was rejected
        BID_EXPIRED,          // Bid listing expired
        ORDER_CREATED,        // New order created
        ORDER_STATUS_CHANGE,  // Order status updated
        PAYMENT_RECEIVED,     // Payment received by farmer
        PAYMENT_REMINDER,     // Payment deadline reminder
        ADMIN_BROADCAST      // System-wide announcement
    }

    public enum Priority {
        MEDIUM
    }
}
