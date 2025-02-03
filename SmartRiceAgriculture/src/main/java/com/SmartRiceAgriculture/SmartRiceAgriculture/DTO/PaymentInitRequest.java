package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Payment;
import lombok.Data;

@Data
public class PaymentInitRequest {
    private Long orderId;
    private Payment.PaymentMethod paymentMethod;
}