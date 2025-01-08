package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import lombok.Data;

@Data
public class OrderPaymentRequest {
    private Order.PaymentMethod paymentMethod;
    private String paymentReference;
}
