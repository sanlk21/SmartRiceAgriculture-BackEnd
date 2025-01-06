package com.SmartRiceAgriculture.SmartRiceAgriculture.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LandDto {
    private Long id;
    private Double size;
    private String location;
    private String district;
    private String soilType;
    private String status;
}