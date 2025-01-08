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
    private String orderNumber; // Format: ORD-2024-001

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
        PENDING_PAYMENT,
        PAYMENT_COMPLETED,
        CANCELLED
    }

    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        BANK_TRANSFER,
        ONLINE_PAYMENT
    }

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
        paymentDeadline = orderDate.plusHours(24); // 24 hours payment deadline
        if (orderNumber == null) {
            // Generate order number: ORD-YYYY-SEQUENCE
            // Note: In a real implementation, you might want to use a more sophisticated sequence generator
            orderNumber = String.format("ORD-%d-%03d",
                    orderDate.getYear(),
                    id != null ? id : 1);
        }
    }
}