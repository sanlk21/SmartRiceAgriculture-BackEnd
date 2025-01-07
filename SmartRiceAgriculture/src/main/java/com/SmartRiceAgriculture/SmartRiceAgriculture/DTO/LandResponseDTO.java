package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;


import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandResponseDTO {
    private Long id;
    private String farmerNic;
    private Float size;
    private String location;
    private String district;
    private String documentName;
    private LandStatus status;
    private Float nitrogenQuota;
    private Float phosphorusQuota;
    private Float potassiumQuota;
    private Float totalNpkQuota;
}
