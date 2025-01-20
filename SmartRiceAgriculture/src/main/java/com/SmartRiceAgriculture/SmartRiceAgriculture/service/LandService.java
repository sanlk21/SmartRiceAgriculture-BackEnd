package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import com.SmartRiceAgriculture.SmartRiceAgriculture.exception.DocumentStorageException;
import com.SmartRiceAgriculture.SmartRiceAgriculture.exception.LandNotFoundException;
import com.SmartRiceAgriculture.SmartRiceAgriculture.mapper.LandConverter;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.LandRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LandService {
    private final LandRepository landRepository;
    private final LandConverter landConverter;
    private static final String UPLOAD_DIR = "uploads/documents";

    @Transactional
    @CacheEvict(value = {"farmerLands", "allLands"}, allEntries = true)
    public LandResponseDTO registerLand(LandRequestDTO dto) {
        log.info("Registering new land for farmer: {}", dto.getFarmerNic());
        validateLandRequest(dto);

        Land land = landConverter.toEntity(dto);

        if (dto.getDocument() != null && !dto.getDocument().isEmpty()) {
            String fileName = handleDocumentUpload(dto);
            land.setDocumentName(fileName);
            land.setDocumentType(dto.getDocument().getContentType());
            land.setDocumentPath(UPLOAD_DIR + "/" + fileName);
        }

        land.calculateFertilizerQuotas();
        Land savedLand = landRepository.save(land);
        log.info("Successfully registered land with ID: {}", savedLand.getId());

        return landConverter.toDTO(savedLand);
    }

    @Transactional
    @CacheEvict(value = {"farmerLands", "allLands"}, allEntries = true)
    public LandResponseDTO updateLandStatus(Long id, LandStatus status) {
        log.info("Updating land status for ID: {} to status: {}", id, status);
        Land land = getLandById(id);
        land.setStatus(status);
        Land updatedLand = landRepository.save(land);
        log.info("Successfully updated land status for ID: {}", id);
        return landConverter.toDTO(updatedLand);
    }

    @Transactional(readOnly = true)
    public LandResponseDTO getLandResponseById(Long id) {
        return landConverter.toDTO(getLandById(id));
    }

    @Transactional(readOnly = true)
    public Land getLandById(Long id) {
        return landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with id: " + id));
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "farmerLands", key = "#farmerNic")
    public List<LandResponseDTO> getFarmerLands(String farmerNic) {
        log.info("Fetching lands for farmer: {}", farmerNic);
        List<Land> lands = landRepository.findByFarmerNic(farmerNic);
        log.info("Found {} lands for farmer: {}", lands.size(), farmerNic);
        return lands.stream()
                .map(landConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "landsByStatus", key = "#status")
    public List<LandResponseDTO> getLandsByStatus(LandStatus status) {
        log.info("Fetching lands by status: {}", status);
        return landRepository.findByStatus(status).stream()
                .map(landConverter::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable("allLands")
    public List<LandResponseDTO> getAllLands() {
        log.info("Fetching all lands");
        return landRepository.findAll().stream()
                .map(landConverter::toDTO)
                .collect(Collectors.toList());
    }

    private String handleDocumentUpload(LandRequestDTO dto) {
        try {
            String fileName = System.currentTimeMillis() + "_" +
                    StringUtils.cleanPath(dto.getDocument().getOriginalFilename());
            Path uploadPath = Paths.get(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(dto.getDocument().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException e) {
            log.error("Failed to store document for land registration", e);
            throw new DocumentStorageException("Could not store the file", e);
        }
    }

    private void validateLandRequest(LandRequestDTO dto) {
        if (dto.getSize() <= 0) {
            throw new IllegalArgumentException("Land size must be greater than 0");
        }
        if (StringUtils.isEmpty(dto.getLocation())) {
            throw new IllegalArgumentException("Location is required");
        }
        if (StringUtils.isEmpty(dto.getDistrict())) {
            throw new IllegalArgumentException("District is required");
        }
        if (StringUtils.isEmpty(dto.getFarmerNic())) {
            throw new IllegalArgumentException("Farmer NIC is required");
        }
    }
}