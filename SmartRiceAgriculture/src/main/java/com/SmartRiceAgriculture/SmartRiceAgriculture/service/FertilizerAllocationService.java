package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.*;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Notification;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.User;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.FertilizerAllocationRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.LandRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.UserRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FertilizerAllocationService {

    private final FertilizerAllocationRepository fertilizerAllocationRepository;
    private final LandRepository landRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // Farmer Methods
    public List<FertilizerAllocationResponse> getFarmerAllocations(String farmerNic) {
        validateFarmer(farmerNic);
        return fertilizerAllocationRepository.findByFarmerNicOrderByDistributionDateDesc(farmerNic)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public FertilizerAllocationResponse getAllocationDetails(Long id) {
        return convertToResponse(findAllocationById(id));
    }

    public List<FertilizerAllocationResponse> getFarmerAllocationHistory(String farmerNic, Integer year, FertilizerAllocation.CultivationSeason season) {
        validateFarmer(farmerNic);
        return fertilizerAllocationRepository.findByFarmerNicAndYearAndSeason(farmerNic, year, season)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public FertilizerAllocationResponse updateCollectionStatus(Long id, FertilizerAllocationStatusUpdateRequest request, String farmerNic) {
        FertilizerAllocation allocation = findAllocationById(id);

        if (!allocation.getFarmerNic().equals(farmerNic)) {
            throw new IllegalArgumentException("Not authorized to update this allocation");
        }

        if (allocation.getStatus() != FertilizerAllocation.Status.READY) {
            throw new IllegalStateException("Allocation is not ready for collection");
        }

        if (request.getStatus() == FertilizerAllocation.Status.COLLECTED) {
            allocation.setStatus(FertilizerAllocation.Status.COLLECTED);
            allocation.setIsCollected(true);
            allocation.setCollectionDate(LocalDateTime.now());

            notificationService.createFertilizerNotification(
                    farmerNic,
                    Notification.NotificationType.FERTILIZER_COLLECTED,
                    allocation.getAllocatedAmount(),
                    allocation.getSeason().toString(),
                    allocation.getYear(),
                    allocation.getDistributionLocation(),
                    allocation.getReferenceNumber()
            );
        }

        return convertToResponse(fertilizerAllocationRepository.save(allocation));
    }

    // Admin Methods
    public Page<FertilizerAllocationResponse> getAllAllocations(Pageable pageable) {
        return fertilizerAllocationRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    public FertilizerAllocationResponse createAllocation(FertilizerAllocationCreateRequest request) {
        User farmer = validateFarmer(request.getFarmerNic());
        Land land = validateLand(request.getLandId(), request.getFarmerNic());

        if (hasActiveAllocation(request.getFarmerNic(), request.getSeason(), request.getYear())) {
            throw new IllegalStateException("Farmer already has an active allocation for this season");
        }

        FertilizerAllocation allocation = new FertilizerAllocation();
        allocation.setFarmerNic(request.getFarmerNic());
        allocation.setLand(land); // Set the Land object
        allocation.setAllocatedAmount(calculateAllocationAmount(land));
        allocation.setSeason(request.getSeason());
        allocation.setYear(request.getYear());
        allocation.setStatus(FertilizerAllocation.Status.PENDING);

        FertilizerAllocation savedAllocation = fertilizerAllocationRepository.save(allocation);

        notificationService.createFertilizerNotification(
                farmer.getNic(),
                Notification.NotificationType.FERTILIZER_ALLOCATED,
                allocation.getAllocatedAmount(),
                allocation.getSeason().toString(),
                allocation.getYear(),
                null,
                null
        );

        return convertToResponse(savedAllocation);
    }

    public FertilizerAllocationResponse updateAllocation(Long id, FertilizerAllocationCreateRequest request) {
        FertilizerAllocation allocation = findAllocationById(id);
        Land land = validateLand(request.getLandId(), request.getFarmerNic());

        if (allocation.getStatus() != FertilizerAllocation.Status.PENDING) {
            throw new IllegalStateException("Cannot update allocation that is not in PENDING status");
        }

        allocation.setLand(land); // Set the Land object
        allocation.setAllocatedAmount(calculateAllocationAmount(land));
        allocation.setSeason(request.getSeason());
        allocation.setYear(request.getYear());

        return convertToResponse(fertilizerAllocationRepository.save(allocation));
    }

    public List<FertilizerAllocationResponse> getSeasonalAllocations(FertilizerAllocation.CultivationSeason season, Integer year) {
        return fertilizerAllocationRepository.findBySeasonAndYear(season, year)
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

    public FertilizerAllocationResponse setDistributionDetails(Long id, FertilizerDistributionRequest request) {
        FertilizerAllocation allocation = findAllocationById(id);

        if (allocation.getStatus() != FertilizerAllocation.Status.PENDING) {
            throw new IllegalStateException("Can only set distribution details for PENDING allocations");
        }

        allocation.setDistributionLocation(request.getDistributionLocation());
        allocation.setReferenceNumber(request.getReferenceNumber());
        allocation.setDistributionDate(LocalDateTime.now());
        allocation.setStatus(FertilizerAllocation.Status.READY);

        FertilizerAllocation savedAllocation = fertilizerAllocationRepository.save(allocation);

        notificationService.createFertilizerNotification(
                allocation.getFarmerNic(),
                Notification.NotificationType.FERTILIZER_READY,
                allocation.getAllocatedAmount(),
                allocation.getSeason().toString(),
                allocation.getYear(),
                request.getDistributionLocation(),
                request.getReferenceNumber()
        );

        return convertToResponse(savedAllocation);
    }

    public FertilizerAllocationStatisticsResponse getStatistics() {
        List<FertilizerAllocation> allocations = fertilizerAllocationRepository.findAll();

        FertilizerAllocationStatisticsResponse stats = new FertilizerAllocationStatisticsResponse();
        stats.setTotalAllocations((long) allocations.size());

        long collectedCount = allocations.stream()
                .filter(FertilizerAllocation::getIsCollected)
                .count();
        stats.setCollectedCount(collectedCount);
        stats.setPendingCount(stats.getTotalAllocations() - collectedCount);

        double totalAmount = allocations.stream()
                .mapToDouble(FertilizerAllocation::getAllocatedAmount)
                .sum();
        double collectedAmount = allocations.stream()
                .filter(FertilizerAllocation::getIsCollected)
                .mapToDouble(FertilizerAllocation::getAllocatedAmount)
                .sum();

        stats.setTotalAmount(totalAmount);
        stats.setCollectedAmount(collectedAmount);
        stats.setPendingAmount(totalAmount - collectedAmount);

        int currentYear = LocalDateTime.now().getYear();
        List<FertilizerAllocation> currentYearAllocations = new ArrayList<>();
        for (FertilizerAllocation allocation : allocations) {
            if (allocation.getYear() == currentYear) {
                currentYearAllocations.add(allocation);
            }
        }


        stats.setCurrentYearAllocations((long) currentYearAllocations.size());
        stats.setCurrentYearAmount(currentYearAllocations.stream()
                .mapToDouble(FertilizerAllocation::getAllocatedAmount)
                .sum());

        return stats;
    }

    // Helper Methods
    private FertilizerAllocation findAllocationById(Long id) {
        return fertilizerAllocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation not found"));
    }

    private User validateFarmer(String farmerNic) {
        return userRepository.findById(farmerNic)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found"));
    }

    private Land validateLand(Long landId, String farmerNic) {
        Land land = landRepository.findById(landId)
                .orElseThrow(() -> new ResourceNotFoundException("Land not found"));

        if (!land.getFarmerNic().equals(farmerNic)) {
            throw new IllegalArgumentException("Land does not belong to farmer");
        }

        return land;
    }

    private boolean hasActiveAllocation(String farmerNic, FertilizerAllocation.CultivationSeason season, Integer year) {
        return fertilizerAllocationRepository.existsByFarmerNicAndSeasonAndYearAndStatusNot(
                farmerNic, season, year, FertilizerAllocation.Status.COLLECTED
        );
    }

    private Float calculateAllocationAmount(Land land) {
        return land.getSize() * 50.0f; // 50kg per hectare
    }

    private FertilizerAllocationResponse convertToResponse(FertilizerAllocation allocation) {
        if (allocation == null) return null;

        FertilizerAllocationResponse response = new FertilizerAllocationResponse();
        response.setId(allocation.getId());
        response.setFarmerNic(allocation.getFarmerNic());
        response.setLandId(allocation.getLand().getId()); // Access Land ID
        response.setAllocatedAmount(allocation.getAllocatedAmount());
        response.setSeason(allocation.getSeason());
        response.setYear(allocation.getYear());
        response.setDistributionDate(allocation.getDistributionDate());
        response.setDistributionLocation(allocation.getDistributionLocation());
        response.setReferenceNumber(allocation.getReferenceNumber());
        response.setIsCollected(allocation.getIsCollected());
        response.setCollectionDate(allocation.getCollectionDate());
        response.setStatus(allocation.getStatus());

        try {
            userRepository.findById(allocation.getFarmerNic())
                    .ifPresent(farmer -> response.setFarmerName(farmer.getFullName()));

            response.setLandLocation(allocation.getLand().getLocation()); // Access Land Location
            response.setLandSize(allocation.getLand().getSize()); // Access Land Size
        } catch (Exception e) {
            System.err.println("Error fetching related entities: " + e.getMessage());
        }

        return response;
    }
}
