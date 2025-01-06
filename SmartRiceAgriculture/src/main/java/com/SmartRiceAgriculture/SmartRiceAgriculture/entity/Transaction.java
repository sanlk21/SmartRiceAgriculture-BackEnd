package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_nic", nullable = false)
    private User buyer;

    private Double quantity; // in kilograms

    private BigDecimal pricePerKg;

    private BigDecimal totalAmount;

    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String paymentReference;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    private String remarks;

    public enum TransactionStatus {
        PENDING, COMPLETED, FAILED
    }

    public enum PaymentMethod {
        BANK_TRANSFER, CASH
    }
}