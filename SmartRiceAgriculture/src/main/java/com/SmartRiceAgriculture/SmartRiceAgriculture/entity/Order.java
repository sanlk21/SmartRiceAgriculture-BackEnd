package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private Long bidId;

    @Column(nullable = false)
    private String buyerNic;

    @Column(nullable = false)
    private String farmerNic;

    @Column(nullable = false)
    private Float quantity;

    @Column(nullable = false)
    private Float pricePerKg;

    @Column(nullable = false)
    private Float totalAmount;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private LocalDateTime paymentDeadline;

    @Column(nullable = false)
    private LocalDateTime harvestDate;

    // Farmer's bank details
    private String farmerBankName;
    private String farmerBankBranch;
    private String farmerAccountNumber;
    private String farmerAccountHolderName;

    // Payment details
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;

    public enum OrderStatus {
        PENDING_PAYMENT,        // Initial state
        PAYMENT_COMPLETED,      // Payment successful
        CANCELLED              // Order cancelled
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        BANK_TRANSFER,
        ONLINE_PAYMENT
    }

    public void markAsCompleted() {
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.paymentDate = LocalDateTime.now();
    }

    public void markAsCancelled() {
        this.status = OrderStatus.CANCELLED;
    }

    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
        if (paymentMethod != null) {
            switch (paymentMethod) {
                case BANK_TRANSFER -> this.paymentMethod = PaymentMethod.BANK_TRANSFER;
                case CASH_ON_DELIVERY -> this.paymentMethod = PaymentMethod.CASH_ON_DELIVERY;
                case ONLINE_PAYMENT -> this.paymentMethod = PaymentMethod.ONLINE_PAYMENT;
            }
        }
    }

    public void updatePaymentDetails(Payment payment) {
        this.paymentMethod = convertPaymentMethod(payment.getPaymentMethod());
        this.paymentReference = payment.getTransactionReference();
        this.paymentDate = LocalDateTime.now();
        this.status = OrderStatus.PAYMENT_COMPLETED;
    }

    private PaymentMethod convertPaymentMethod(Payment.PaymentMethod paymentMethod) {
        return switch (paymentMethod) {
            case BANK_TRANSFER -> PaymentMethod.BANK_TRANSFER;
            case CASH_ON_DELIVERY -> PaymentMethod.CASH_ON_DELIVERY;
            case ONLINE_PAYMENT -> PaymentMethod.ONLINE_PAYMENT;
        };
    }

    public void completePayment(Payment payment) {
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.paymentDate = LocalDateTime.now();
        this.paymentMethod = convertPaymentMethod(payment.getPaymentMethod());
        this.paymentReference = payment.getTransactionReference();
    }

    public void updatePaymentStatus(Payment payment) {
        switch (payment.getStatus()) {
            case COMPLETED -> {
                this.status = OrderStatus.PAYMENT_COMPLETED;
                this.paymentDate = LocalDateTime.now();
            }
            case CANCELLED -> this.status = OrderStatus.CANCELLED;
            default -> this.status = OrderStatus.PENDING_PAYMENT;
        }
    }

    public void setHarvestDateFromBid(LocalDateTime harvestDate) {
        this.harvestDate = harvestDate;
    }

    @PrePersist
    protected void onCreate() {
        // Set order date if not set
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }

        // Set payment deadline to 24 hours from order date
        if (paymentDeadline == null) {
            paymentDeadline = orderDate.plusHours(24);
        }

        // Set default harvest date if not set
        if (harvestDate == null) {
            harvestDate = LocalDateTime.now().plusDays(30);
        }

        // Generate order number
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }

        // Calculate total amount if not set
        if (totalAmount == null && quantity != null && pricePerKg != null) {
            totalAmount = quantity * pricePerKg;
        }
    }

    private String generateOrderNumber() {
        return String.format("ORD-%d%02d%02d%02d%02d%02d",
                orderDate.getYear(),
                orderDate.getMonthValue(),
                orderDate.getDayOfMonth(),
                orderDate.getHour(),
                orderDate.getMinute(),
                orderDate.getSecond());
    }
}