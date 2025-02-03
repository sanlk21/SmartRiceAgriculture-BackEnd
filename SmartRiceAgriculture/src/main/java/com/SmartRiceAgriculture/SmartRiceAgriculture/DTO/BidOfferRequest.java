package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import lombok.Data;

// BidOfferRequest.java
@Data
public class BidOfferRequest {
    private Long bidId;
    private String buyerNic;  // Add this field
    private Float bidAmount;
}
