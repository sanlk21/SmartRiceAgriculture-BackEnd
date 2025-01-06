package com.SmartRiceAgriculture.SmartRiceAgriculture.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FertilizerQuotaDto {
    private Long id;
    private Double allocatedAmount;
    private String allocationDate;
    private String expiryDate;
    private Boolean isCollected;
    private String season;
    private String status;
}