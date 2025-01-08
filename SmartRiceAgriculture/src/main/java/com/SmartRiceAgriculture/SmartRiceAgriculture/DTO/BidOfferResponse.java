package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;

import java.time.LocalDateTime;

// BidOfferResponse.java
@Data
public class BidOfferResponse {
    private String buyerNic;
    private Float bidAmount;
    private LocalDateTime bidDate;
}
