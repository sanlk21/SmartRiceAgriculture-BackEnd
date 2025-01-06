package com.SmartRiceAgriculture.SmartRiceAgriculture.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RiceVarietyDto {
    private Long id;
    private String varietyName;
    private Integer growingDuration;
    private Double expectedYieldPerHectare;
    private String description;
    private String grade;
}