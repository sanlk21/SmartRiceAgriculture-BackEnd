package com.SmartRiceAgriculture.SmartRiceAgriculture.service;


import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.FertilizerAllocationRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.LandRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FertilizerAllocationService {
    private final FertilizerAllocationRepository fertilizerAllocationRepository;
    private final LandRepository landRepository;
    private final UserRepository userRepository;

    public FertilizerAllocationResponse createAllocation(FertilizerAllocationCreateRequest request) {
        // Validate farmer exists
        User farmer = userRepository.findById(request.getFarmerNic())
                .orElseThrow(() -> new EntityNotFoundException("Farmer not found"));

        // Validate land exists and belongs to farmer
        Land land = landRepository.findById(request.getLandId())
                .orElseThrow(() -> new EntityNotFoundException("Land not found"));

        if (!land.getFarmerNic().equals(request.getFarmerNic())) {
            throw new IllegalArgumentException("Land does not belong to farmer");
        }

        // Create allocation
        FertilizerAllocation allocation = new FertilizerAllocation();
        allocation.setFarmerNic(request.getFarmerNic());
        allocation.setLandId(request.getLandId());
        allocation.setAllocatedAmount(land.getTotalNpkQuota());
        allocation.setSeason(request.getSeason());
        allocation.setYear(request.getYear());
        allocation.setStatus(FertilizerAllocation.Status.PENDING);
        allocation.setIsCollected(false);

        FertilizerAllocation savedAllocation = fertilizerAllocationRepository.save(allocation);
        return convertToResponse(savedAllocation);
    }

    public FertilizerAllocationResponse updateStatus(Long id, FertilizerAllocationStatusUpdateRequest request) {
        FertilizerAllocation allocation = fertilizerAllocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Allocation not found"));

        allocation.setStatus(request.getStatus());

        if (request.getStatus() == FertilizerAllocation.Status.COLLECTED) {
            allocation.setIsCollected(true);
            allocation.setCollectionDate(LocalDateTime.now());
        }

        return convertToResponse(fertilizerAllocationRepository.save(allocation));
    }

    public FertilizerAllocationResponse setDistributionDetails(Long id, FertilizerDistributionRequest request) {
        FertilizerAllocation allocation = fertilizerAllocationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Allocation not found"));

        allocation.setDistributionLocation(request.getDistributionLocation());
        allocation.setReferenceNumber(request.getReferenceNumber());
        allocation.setDistributionDate(LocalDateTime.now());
        allocation.setStatus(FertilizerAllocation.Status.READY);

        return convertToResponse(fertilizerAllocationRepository.save(allocation));
    }

    public List<FertilizerAllocationResponse> getFarmerAllocations(String farmerNic) {
        return fertilizerAllocationRepository.findByFarmerNic(farmerNic)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<FertilizerAllocationResponse> getAllocationsByStatus(FertilizerAllocation.Status status) {
        return fertilizerAllocationRepository.findByStatus(status)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public FertilizerAllocationStatisticsResponse getStatistics() {
        List<FertilizerAllocation> allocations = fertilizerAllocationRepository.findAll();

        FertilizerAllocationStatisticsResponse stats = new FertilizerAllocationStatisticsResponse();

        // Total allocations
        stats.setTotalAllocations((long) allocations.size());

        // Collected vs Pending
        long collectedCount = allocations.stream()
                .filter(FertilizerAllocation::getIsCollected)
                .count();
        stats.setCollectedCount(collectedCount);
        stats.setPendingCount((long) allocations.size() - collectedCount);

        // Amount calculations
        double totalAmount = allocations.stream()
                .mapToDouble(a -> a.getAllocatedAmount() != null ? a.getAllocatedAmount() : 0.0)
                .sum();
        stats.setTotalAmount(totalAmount);

        double collectedAmount = allocations.stream()
                .filter(FertilizerAllocation::getIsCollected)
                .mapToDouble(a -> a.getAllocatedAmount() != null ? a.getAllocatedAmount() : 0.0)
                .sum();
        stats.setCollectedAmount(collectedAmount);
        stats.setPendingAmount(totalAmount - collectedAmount);

        // Current year statistics
        int currentYear = LocalDateTime.now().getYear();
        List<FertilizerAllocation> currentYearAllocations = allocations.stream()
                .filter(a -> a.getYear() != null && a.getYear() == currentYear)
                .collect(Collectors.toList());

        stats.setCurrentYearAllocations((long) currentYearAllocations.size());
        stats.setCurrentYearAmount(currentYearAllocations.stream()
                .mapToDouble(a -> a.getAllocatedAmount() != null ? a.getAllocatedAmount() : 0.0)
                .sum());

        return stats;
    }

    private FertilizerAllocationResponse convertToResponse(FertilizerAllocation allocation) {
        if (allocation == null) {
            return null;
        }

        FertilizerAllocationResponse response = new FertilizerAllocationResponse();
        response.setId(allocation.getId());
        response.setFarmerNic(allocation.getFarmerNic());
        response.setLandId(allocation.getLandId());
        response.setAllocatedAmount(allocation.getAllocatedAmount());
        response.setSeason(allocation.getSeason());
        response.setYear(allocation.getYear());
        response.setDistributionDate(allocation.getDistributionDate());
        response.setDistributionLocation(allocation.getDistributionLocation());
        response.setReferenceNumber(allocation.getReferenceNumber());
        response.setIsCollected(allocation.getIsCollected());
        response.setCollectionDate(allocation.getCollectionDate());
        response.setStatus(allocation.getStatus());

        // Get additional details from related entities
        try {
            // Get farmer name
            userRepository.findById(allocation.getFarmerNic()).ifPresent(farmer ->
                    response.setFarmerName(farmer.getFullName())
            );

            // Get land details
            landRepository.findById(allocation.getLandId()).ifPresent(land -> {
                response.setLandLocation(land.getLocation());
                response.setLandSize(land.getSize());
            });
        } catch (Exception e) {
            // Log error but don't fail the response
            System.err.println("Error fetching related entities: " + e.getMessage());
        }

        return response;
    }
}
