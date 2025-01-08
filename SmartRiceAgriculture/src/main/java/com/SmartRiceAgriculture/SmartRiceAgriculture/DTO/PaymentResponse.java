package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private Long orderId;
    private String buyerNic;
    private String farmerNic;
    private Float amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus status;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Integer attemptCount;
    private PaymentDetailsResponse details;
}

