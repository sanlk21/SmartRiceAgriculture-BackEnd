package com.SmartRiceAgriculture.SmartRiceAgriculture.service;


import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import com.SmartRiceAgriculture.SmartRiceAgriculture.mapper.LandConverter;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.LandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LandService {
    private final LandRepository landRepository;
    private final LandConverter landConverter;
    private final String UPLOAD_DIR = "uploads/documents";

    public LandResponseDTO registerLand(LandRequestDTO dto) {
        Land land = landConverter.toEntity(dto);

        // Handle document upload if present
        if (dto.getDocument() != null && !dto.getDocument().isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" +
                        StringUtils.cleanPath(dto.getDocument().getOriginalFilename());
                Path uploadPath = Paths.get(UPLOAD_DIR);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(dto.getDocument().getInputStream(), filePath);

                land.setDocumentName(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Could not store the file", e);
            }
        }

        land.calculateFertilizerQuotas();
        Land savedLand = landRepository.save(land);
        return landConverter.toDTO(savedLand);
    }

    public LandResponseDTO updateLandStatus(Long id, LandStatus status) {
        Land land = getLandById(id);
        land.setStatus(status);
        return landConverter.toDTO(landRepository.save(land));
    }

    public LandResponseDTO getLandResponseById(Long id) {
        return landConverter.toDTO(getLandById(id));
    }

    public Land getLandById(Long id) {
        return landRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Land not found with id: " + id));
    }

    public List<LandResponseDTO> getFarmerLands(String farmerNic) {
        return landRepository.findByFarmerNic(farmerNic).stream()
                .map(landConverter::toDTO)
                .collect(Collectors.toList());
    }

    public List<LandResponseDTO> getLandsByStatus(LandStatus status) {
        return landRepository.findByStatus(status).stream()
                .map(landConverter::toDTO)
                .collect(Collectors.toList());
    }

    public List<LandResponseDTO> getAllLands() {
        return landRepository.findAll().stream()
                .map(landConverter::toDTO)
                .collect(Collectors.toList());
    }
}
