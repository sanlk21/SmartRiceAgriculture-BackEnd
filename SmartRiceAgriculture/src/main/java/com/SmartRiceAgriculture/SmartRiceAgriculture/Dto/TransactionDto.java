package com.SmartRiceAgriculture.SmartRiceAgriculture.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private Long id;
    private Double quantity;
    private Double amount;
    private String paymentMethod;
    private String paymentReference;
    private String transactionDate;
    private String status;
}