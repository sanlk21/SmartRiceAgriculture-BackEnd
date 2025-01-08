package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;


import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid.RiceVariety;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BidCreateRequest {
    private Float quantity;
    private Float minimumPrice;
    private RiceVariety riceVariety;
    private String description;
    private String location;
}

