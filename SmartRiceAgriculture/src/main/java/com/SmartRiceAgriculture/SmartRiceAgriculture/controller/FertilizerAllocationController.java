package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.FertilizerAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fertilizer")
@RequiredArgsConstructor
public class FertilizerAllocationController {
    private final FertilizerAllocationService fertilizerService;

    @PostMapping("/allocate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FertilizerAllocationResponse> createAllocation(
            @RequestBody FertilizerAllocationCreateRequest request) {
        return ResponseEntity.ok(fertilizerService.createAllocation(request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FertilizerAllocationResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody FertilizerAllocationStatusUpdateRequest request) {
        return ResponseEntity.ok(fertilizerService.updateStatus(id, request));
    }

    @PutMapping("/{id}/distribution")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FertilizerAllocationResponse> setDistributionDetails(
            @PathVariable Long id,
            @RequestBody FertilizerDistributionRequest request) {
        return ResponseEntity.ok(fertilizerService.setDistributionDetails(id, request));
    }

    @GetMapping("/farmer/{farmerNic}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FARMER')")
    public ResponseEntity<List<FertilizerAllocationResponse>> getFarmerAllocations(
            @PathVariable String farmerNic) {
        return ResponseEntity.ok(fertilizerService.getFarmerAllocations(farmerNic));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FertilizerAllocationResponse>> getAllocationsByStatus(
            @PathVariable FertilizerAllocation.Status status) {
        return ResponseEntity.ok(fertilizerService.getAllocationsByStatus(status));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FertilizerAllocationStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(fertilizerService.getStatistics());
    }
}
