package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface FertilizerAllocationRepository extends JpaRepository<FertilizerAllocation, Long> {
    List<FertilizerAllocation> findByFarmerNic(String farmerNic);

    List<FertilizerAllocation> findByLandId(Long landId);

    List<FertilizerAllocation> findByStatus(FertilizerAllocation.Status status);

    List<FertilizerAllocation> findBySeasonAndYear(
            FertilizerAllocation.CultivationSeason season,
            Integer year
    );

    List<FertilizerAllocation> findByFarmerNicAndSeasonAndYear(
            String farmerNic,
            FertilizerAllocation.CultivationSeason season,
            Integer year
    );

    @Query("SELECT fa FROM FertilizerAllocation fa WHERE fa.farmerNic = ?1 AND fa.isCollected = ?2")
    List<FertilizerAllocation> findByFarmerNicAndCollectionStatus(String farmerNic, Boolean isCollected);

    @Query("SELECT COUNT(fa) > 0 FROM FertilizerAllocation fa WHERE fa.farmerNic = ?1 AND fa.season = ?2 AND fa.year = ?3")
    boolean hasAllocationForSeason(String farmerNic, FertilizerAllocation.CultivationSeason season, Integer year);
}
