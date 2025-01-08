package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;


import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation.CultivationSeason;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerAllocationCreateRequest {
    private String farmerNic;
    private Long landId;
    private CultivationSeason season;
    private Integer year;
}



