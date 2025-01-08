package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDetailsResponse {
    // Bank Transfer Details
    private String senderBankName;
    private String senderAccountNumber;
    private String senderAccountName;
    private LocalDateTime transferDate;

    // Cash on Delivery Details
    private String deliveryAddress;
    private LocalDateTime scheduledDeliveryDate;
    private String deliveryAgentName;
    private String deliveryContactNumber;

    // Online Payment Details
    private String paymentGatewayName;
    private String paymentGatewayTransactionId;
    private String paymentGatewayStatus;
}
