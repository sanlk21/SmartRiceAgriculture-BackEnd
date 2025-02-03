package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

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
    private String paymentNumber;

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

    private String transactionReference;
    private String paymentProofDocumentPath;

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

    @Transient
    private static final Object SEQUENCE_LOCK = new Object();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        setInitialStatus();
    }

    public void setInitialStatus() {
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
        if (attemptCount == null) {
            attemptCount = 0;
        }
    }

    public void recordAttempt(String failureReason) {
        this.attemptCount++;
        this.lastAttemptAt = LocalDateTime.now();
        this.failureReason = failureReason;
    }

    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return switch (paymentMethod) {
            case BANK_TRANSFER -> senderBankName != null &&
                    senderAccountNumber != null &&
                    transferDate != null;
            case CASH_ON_DELIVERY -> deliveryAddress != null &&
                    scheduledDeliveryDate != null;
            case ONLINE_PAYMENT -> paymentGatewayName != null;
        };
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        BANK_TRANSFER,
        ONLINE_PAYMENT
    }

    public enum PaymentStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED,
        REFUNDED
    }
}
