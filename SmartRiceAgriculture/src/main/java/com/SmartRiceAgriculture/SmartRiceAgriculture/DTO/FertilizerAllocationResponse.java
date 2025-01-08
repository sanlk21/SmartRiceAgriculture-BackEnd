package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation.CultivationSeason;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerAllocationResponse {
    private Long id;
    private String farmerNic;
    private String farmerName;
    private Long landId;
    private String landLocation;
    private Float landSize;
    private Float allocatedAmount;
    private CultivationSeason season;
    private Integer year;
    private LocalDateTime distributionDate;
    private String distributionLocation;
    private String referenceNumber;
    private Boolean isCollected;
    private LocalDateTime collectionDate;
    private Status status;
}
