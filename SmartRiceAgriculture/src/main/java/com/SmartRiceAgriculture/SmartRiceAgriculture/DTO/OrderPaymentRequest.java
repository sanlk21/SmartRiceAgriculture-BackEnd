package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import lombok.Data;

@Data
public class OrderPaymentRequest {
    private Payment.PaymentMethod paymentMethod;
    private String paymentReference;
}