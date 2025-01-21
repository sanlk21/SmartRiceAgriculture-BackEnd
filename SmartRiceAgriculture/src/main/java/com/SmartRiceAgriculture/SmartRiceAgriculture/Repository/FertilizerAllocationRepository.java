package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.FertilizerAllocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FertilizerAllocationRepository extends JpaRepository<FertilizerAllocation, Long> {

    // Find by farmer NIC ordered by distribution date
    List<FertilizerAllocation> findByFarmerNicOrderByDistributionDateDesc(String farmerNic);

    // Find by season and year
    List<FertilizerAllocation> findBySeasonAndYear(
            FertilizerAllocation.CultivationSeason season,
            Integer year
    );

    // Find by farmer NIC, year and season
    List<FertilizerAllocation> findByFarmerNicAndYearAndSeason(
            String farmerNic,
            Integer year,
            FertilizerAllocation.CultivationSeason season
    );

    // Find by status
    List<FertilizerAllocation> findByStatus(FertilizerAllocation.Status status);

    // Check for existing active allocation
    @Query("SELECT CASE WHEN COUNT(fa) > 0 THEN true ELSE false END FROM FertilizerAllocation fa " +
            "WHERE fa.farmerNic = ?1 AND fa.season = ?2 AND fa.year = ?3 AND fa.status != ?4")
    boolean existsByFarmerNicAndSeasonAndYearAndStatusNot(
            String farmerNic,
            FertilizerAllocation.CultivationSeason season,
            Integer year,
            FertilizerAllocation.Status status
    );

    // Check if allocation exists for a specific land
    boolean existsByLandId(Long landId);
}
