package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandRequestDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.LandResponseDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.LandService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lands")
@RequiredArgsConstructor
public class LandController {
    private final LandService landService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LandResponseDTO> registerLand(@ModelAttribute LandRequestDTO request) {
        return ResponseEntity.ok(landService.registerLand(request));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<LandResponseDTO> updateStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest status) {
        LandResponseDTO response = landService.updateLandStatus(id, status.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandResponseDTO> getLand(@PathVariable Long id) {
        return ResponseEntity.ok(landService.getLandResponseById(id));
    }

    @GetMapping("/farmer/{farmerNic}")
    public ResponseEntity<List<LandResponseDTO>> getFarmerLands(@PathVariable String farmerNic) {
        return ResponseEntity.ok(landService.getFarmerLands(farmerNic));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LandResponseDTO>> getLandsByStatus(@PathVariable LandStatus status) {
        return ResponseEntity.ok(landService.getLandsByStatus(status));
    }

    @GetMapping
    public ResponseEntity<List<LandResponseDTO>> getAllLands() {
        return ResponseEntity.ok(landService.getAllLands());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        private LandStatus status;
    }
}
