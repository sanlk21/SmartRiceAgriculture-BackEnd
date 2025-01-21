package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FertilizerAllocationCreateRequest {
    private String farmerNic;
    private Long landId;
    private FertilizerAllocation.CultivationSeason season;
    private Integer year;
    private FertilizerAllocation.Status status; // Add this property

    public FertilizerAllocation.Status getStatus() {
        return status;
    }
}
