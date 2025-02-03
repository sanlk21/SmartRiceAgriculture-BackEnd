package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BidCreateRequest {
    private String farmerNic;
    private Float quantity;
    private Float minimumPrice;
    private Bid.RiceVariety riceVariety;
    private String description;
    private String location;
    private LocalDateTime harvestDate;
}