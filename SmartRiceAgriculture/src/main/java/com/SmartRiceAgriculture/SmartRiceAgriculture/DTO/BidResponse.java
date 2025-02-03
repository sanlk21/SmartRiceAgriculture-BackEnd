package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BidResponse {
    private Long id;
    private String farmerNic;
    private Float quantity;
    private Float minimumPrice;
    private Bid.RiceVariety riceVariety;
    private String description;
    private String location;
    private LocalDateTime postedDate;
    private LocalDateTime expiryDate;
    private LocalDateTime harvestDate;  // Added this field
    private Bid.BidStatus status;
    private List<BidOfferResponse> bidOffers;
    private String winningBuyerNic;
    private Float winningBidAmount;
    private LocalDateTime winningBidDate;
}