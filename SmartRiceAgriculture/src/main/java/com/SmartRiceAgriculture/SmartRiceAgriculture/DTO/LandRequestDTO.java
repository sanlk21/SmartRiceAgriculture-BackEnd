package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandRequestDTO {
    private String farmerNic;
    private Float size;
    private String location;
    private String district;
    private MultipartFile document;
}
