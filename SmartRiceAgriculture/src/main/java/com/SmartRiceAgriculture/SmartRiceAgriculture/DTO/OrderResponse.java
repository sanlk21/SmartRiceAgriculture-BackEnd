package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalDate harvestDate;  // Added this field
    private String farmerBankName;
    private String farmerBankBranch;
    private String farmerAccountNumber;
    private String farmerAccountHolderName;
    private Order.PaymentMethod paymentMethod;
    private String paymentReference;
    private LocalDateTime paymentDate;
    private Order.OrderStatus status;
}