package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String paymentNumber; // Format: PAY-2024-001

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private String buyerNic;

    @Column(nullable = false)
    private String farmerNic;

    @Column(nullable = false)
    private Float amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionReference; // Bank reference, payment gateway ID, etc.
    private String paymentProofDocumentPath; // Path to uploaded proof document

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
    private LocalDateTime lastAttemptAt;
    private Integer attemptCount = 0;

    private String failureReason;
    private String adminNotes;

    // Bank transfer specific fields
    private String senderBankName;
    private String senderAccountNumber;
    private String senderAccountName;
    private LocalDateTime transferDate;

    // Cash on delivery specific fields
    private String deliveryAddress;
    private LocalDateTime scheduledDeliveryDate;
    private String deliveryAgentName;
    private String deliveryContactNumber;

    // Online payment specific fields
    private String paymentGatewayName;
    private String paymentGatewayTransactionId;
    private String paymentGatewayStatus;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentNumber == null) {
            paymentNumber = String.format("PAY-%d-%03d",
                    createdAt.getYear(),
                    id != null ? id : 1);
        }
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        BANK_TRANSFER,
        ONLINE_PAYMENT
    }

    public enum PaymentStatus {
        PENDING,         // Initial state
        PROCESSING,      // Payment is being processed
        COMPLETED,       // Payment successfully completed
        FAILED,         // Payment attempt failed
        CANCELLED,      // Payment cancelled by user or admin
        REFUNDED        // Payment was refunded
    }

    // Method to update attempt count and time
    public void recordAttempt(String failureReason) {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
        this.failureReason = failureReason;
    }

    // Method to mark payment as completed
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    // Method to validate payment based on method
    public boolean isValid() {
        switch (paymentMethod) {
            case BANK_TRANSFER:
                return senderBankName != null &&
                        senderAccountNumber != null &&
                        transferDate != null;

            case CASH_ON_DELIVERY:
                return deliveryAddress != null &&
                        scheduledDeliveryDate != null;

            case ONLINE_PAYMENT:
                return paymentGatewayName != null;

            default:
                return false;
        }
    }
}
