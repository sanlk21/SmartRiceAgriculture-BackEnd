package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.FertilizerAllocationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fertilizer")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class FertilizerAllocationController {

    private static final Logger logger = LoggerFactory.getLogger(FertilizerAllocationController.class);

    private final FertilizerAllocationService fertilizerService;

    // Fetch Allocations for a Farmer
    @GetMapping("/my-allocations/{nic}")
    public ResponseEntity<List<FertilizerAllocationResponse>> getMyAllocations(@PathVariable String nic) {
        logger.info("Fetching allocations for farmer NIC: {}", nic);
        List<FertilizerAllocationResponse> allocations = fertilizerService.getFarmerAllocations(nic);
        return ResponseEntity.ok(allocations);
    }

    // Get Allocation Details by ID
    @GetMapping("/allocations/{id}")
    public ResponseEntity<FertilizerAllocationResponse> getAllocationDetails(@PathVariable Long id) {
        logger.info("Fetching allocation details for ID: {}", id);
        FertilizerAllocationResponse allocation = fertilizerService.getAllocationDetails(id);
        return ResponseEntity.ok(allocation);
    }

    // Fetch Allocation History
    @GetMapping("/history/{nic}")
    public ResponseEntity<List<FertilizerAllocationResponse>> getAllocationHistory(
            @PathVariable String nic,
            @RequestParam Integer year,
            @RequestParam FertilizerAllocation.CultivationSeason season) {
        logger.info("Fetching allocation history for NIC: {}, Year: {}, Season: {}", nic, year, season);
        List<FertilizerAllocationResponse> history = fertilizerService.getFarmerAllocationHistory(nic, year, season);
        return ResponseEntity.ok(history);
    }

    // Update Collection Status
    @PutMapping("/allocations/{id}/status/{nic}")
    public ResponseEntity<FertilizerAllocationResponse> updateCollectionStatus(
            @PathVariable Long id,
            @PathVariable String nic,
            @RequestBody FertilizerAllocationStatusUpdateRequest request) {
        logger.info("Updating collection status for Allocation ID: {} by Farmer NIC: {}", id, nic);
        FertilizerAllocationResponse response = fertilizerService.updateCollectionStatus(id, request, nic);
        return ResponseEntity.ok(response);
    }

    // Admin Endpoints

    // Get All Allocations (Paginated)
    @GetMapping("/admin/allocations")
    public ResponseEntity<Page<FertilizerAllocationResponse>> getAllAllocations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("Fetching all allocations with page: {}, size: {}", page, size);
        Page<FertilizerAllocationResponse> allocations = fertilizerService.getAllAllocations(PageRequest.of(page, size));
        return ResponseEntity.ok(allocations);
    }

    // Create a New Allocation
    @PostMapping("/admin/allocations")
    public ResponseEntity<FertilizerAllocationResponse> createAllocation(
            @RequestBody FertilizerAllocationCreateRequest request) {
        logger.info("Creating allocation for Farmer NIC: {}", request.getFarmerNic());
        FertilizerAllocationResponse allocation = fertilizerService.createAllocation(request);
        return ResponseEntity.ok(allocation);
    }

    // Update Existing Allocation
    @PutMapping("/admin/allocations/{id}")
    public ResponseEntity<FertilizerAllocationResponse> updateAllocation(
            @PathVariable Long id,
            @RequestBody FertilizerAllocationCreateRequest request) {
        logger.info("Updating allocation with ID: {}", id);
        FertilizerAllocationResponse allocation = fertilizerService.updateAllocation(id, request);
        return ResponseEntity.ok(allocation);
    }

    // Get Allocation Statistics
    @GetMapping("/admin/statistics")
    public ResponseEntity<FertilizerAllocationStatisticsResponse> getStatistics() {
        logger.info("Fetching allocation statistics");
        FertilizerAllocationStatisticsResponse stats = fertilizerService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    // Fetch Seasonal Allocations
    @GetMapping("/admin/seasonal")
    public ResponseEntity<List<FertilizerAllocationResponse>> getSeasonalAllocations(
            @RequestParam FertilizerAllocation.CultivationSeason season,
            @RequestParam Integer year) {
        logger.info("Fetching seasonal allocations for Season: {}, Year: {}", season, year);
        List<FertilizerAllocationResponse> allocations = fertilizerService.getSeasonalAllocations(season, year);
        return ResponseEntity.ok(allocations);
    }

    // Set Distribution Details
    @PutMapping("/admin/allocations/{id}/distribution")
    public ResponseEntity<FertilizerAllocationResponse> setDistributionDetails(
            @PathVariable Long id,
            @RequestBody FertilizerDistributionRequest request) {
        logger.info("Setting distribution details for Allocation ID: {}", id);
        FertilizerAllocationResponse allocation = fertilizerService.setDistributionDetails(id, request);
        return ResponseEntity.ok(allocation);
    }

    // Fetch Allocations by Status
    @GetMapping("/admin/allocations/status/{status}")
    public ResponseEntity<List<FertilizerAllocationResponse>> getAllocationsByStatus(
            @PathVariable FertilizerAllocation.Status status) {
        logger.info("Fetching allocations with Status: {}", status);
        List<FertilizerAllocationResponse> allocations = fertilizerService.getAllocationsByStatus(status);
        return ResponseEntity.ok(allocations);
    }
}
