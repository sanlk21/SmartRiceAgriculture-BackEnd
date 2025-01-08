package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order.OrderStatus;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order.PaymentMethod;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long bidId;
    private String buyerNic;
    private String farmerNic;
    private Float quantity;
    private Float pricePerKg;
    private Float totalAmount;
    private LocalDateTime orderDate;
    private LocalDateTime paymentDeadline;
    private String farmerBankName;
    private String farmerBankBranch;
    private String farmerAccountNumber;
    private String farmerAccountHolderName;
    private PaymentMethod paymentMethod;
    private String paymentReference;
    private LocalDateTime paymentDate;
    private OrderStatus status;
}

