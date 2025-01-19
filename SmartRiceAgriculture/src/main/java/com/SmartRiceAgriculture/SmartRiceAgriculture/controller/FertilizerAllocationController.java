package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.FertilizerAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fertilizer")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class FertilizerAllocationController {
    private final FertilizerAllocationService fertilizerService;

    // Farmer Endpoints
    @GetMapping("/my-allocations")
    public ResponseEntity<List<FertilizerAllocationResponse>> getMyAllocations() {
        // Replace farmerNic with a default or placeholder value
        String farmerNic = "defaultFarmerNic";
        return ResponseEntity.ok(fertilizerService.getFarmerAllocations(farmerNic));
    }

    @GetMapping("/allocations/{id}")
    public ResponseEntity<FertilizerAllocationResponse> getAllocationDetails(@PathVariable Long id) {
        return ResponseEntity.ok(fertilizerService.getAllocationDetails(id));
    }

    @GetMapping("/history")
    public ResponseEntity<List<FertilizerAllocationResponse>> getAllocationHistory(
            @RequestParam Integer year,
            @RequestParam FertilizerAllocation.CultivationSeason season) {
        // Replace farmerNic with a default or placeholder value
        String farmerNic = "defaultFarmerNic";
        return ResponseEntity.ok(fertilizerService.getFarmerAllocationHistory(farmerNic, year, season));
    }

    @PutMapping("/allocations/{id}/status")
    public ResponseEntity<FertilizerAllocationResponse> updateCollectionStatus(
            @PathVariable Long id,
            @RequestBody FertilizerAllocationStatusUpdateRequest request) {
        // Replace farmerNic with a default or placeholder value
        String farmerNic = "defaultFarmerNic";
        return ResponseEntity.ok(fertilizerService.updateCollectionStatus(id, request, farmerNic));
    }

    // Admin Endpoints
    @GetMapping("/admin/allocations")
    public ResponseEntity<Page<FertilizerAllocationResponse>> getAllAllocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(fertilizerService.getAllAllocations(PageRequest.of(page, size)));
    }

    @PostMapping("/admin/allocations")
    public ResponseEntity<FertilizerAllocationResponse> createAllocation(
            @RequestBody FertilizerAllocationCreateRequest request) {
        return ResponseEntity.ok(fertilizerService.createAllocation(request));
    }

    @PutMapping("/admin/allocations/{id}")
    public ResponseEntity<FertilizerAllocationResponse> updateAllocation(
            @PathVariable Long id,
            @RequestBody FertilizerAllocationCreateRequest request) {
        return ResponseEntity.ok(fertilizerService.updateAllocation(id, request));
    }

    @GetMapping("/admin/statistics")
    public ResponseEntity<FertilizerAllocationStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(fertilizerService.getStatistics());
    }

    @GetMapping("/admin/seasonal")
    public ResponseEntity<List<FertilizerAllocationResponse>> getSeasonalAllocations(
            @RequestParam FertilizerAllocation.CultivationSeason season,
            @RequestParam Integer year) {
        return ResponseEntity.ok(fertilizerService.getSeasonalAllocations(season, year));
    }

    @PutMapping("/admin/allocations/{id}/distribution")
    public ResponseEntity<FertilizerAllocationResponse> setDistributionDetails(
            @PathVariable Long id,
            @RequestBody FertilizerDistributionRequest request) {
        return ResponseEntity.ok(fertilizerService.setDistributionDetails(id, request));
    }

    @GetMapping("/admin/allocations/status/{status}")
    public ResponseEntity<List<FertilizerAllocationResponse>> getAllocationsByStatus(
            @PathVariable FertilizerAllocation.Status status) {
        return ResponseEntity.ok(fertilizerService.getAllocationsByStatus(status));
    }
}
