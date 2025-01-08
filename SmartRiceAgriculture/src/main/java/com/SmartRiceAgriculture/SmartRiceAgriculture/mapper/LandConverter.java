package com.SmartRiceAgriculture.SmartRiceAgriculture.mapper;


import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import org.springframework.stereotype.Component;

import static com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus.APPROVED;
import static com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus.REJECTED;

@Component
public class LandConverter {

    public Land toEntity(LandRequestDTO dto) {
        Land land = new Land();
        land.setFarmerNic(dto.getFarmerNic());
        land.setSize(dto.getSize());
        land.setLocation(dto.getLocation());
        land.setDistrict(dto.getDistrict());
        land.setStatus(LandStatus.PENDING);
//        land.setStatus(LandStatus.APPROVED);
//        land.setStatus(LandStatus.REJECTED);
        return land;
    }

    public LandResponseDTO toDTO(Land land) {
        return new LandResponseDTO(
                land.getId(),
                land.getFarmerNic(),
                land.getSize(),
                land.getLocation(),
                land.getDistrict(),
                land.getDocumentName(),
                land.getStatus(),
                land.getNitrogenQuota(),
                land.getPhosphorusQuota(),
                land.getPotassiumQuota(),
                land.getTotalNpkQuota()
        );
    }
}
