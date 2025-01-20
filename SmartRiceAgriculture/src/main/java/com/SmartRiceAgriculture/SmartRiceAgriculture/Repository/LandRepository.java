package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandRepository extends JpaRepository<Land, Long> {

    // Find lands by farmer NIC
    List<Land> findByFarmerNic(String farmerNic);

    // Find lands by status
    List<Land> findByStatus(LandStatus status);

    // Find lands by district
    List<Land> findByDistrict(String district);

    // Find lands by farmer NIC and status
    List<Land> findByFarmerNicAndStatus(String farmerNic, LandStatus status);

    // Find lands with quota above a threshold
    @Query("SELECT l FROM Land l WHERE l.totalNpkQuota > ?1")
    List<Land> findLandsWithQuotaAbove(Float threshold);

    // Find lands by district within a size range
    @Query("SELECT l FROM Land l WHERE l.district = ?1 AND l.size BETWEEN ?2 AND ?3")
    List<Land> findByDistrictAndSizeRange(String district, Float minSize, Float maxSize);

    // Count lands in a specific district
    @Query("SELECT COUNT(l) FROM Land l WHERE l.district = ?1")
    long countLandsByDistrict(String district);

    // Retrieve all active lands (not in pending or inactive status)
    @Query("SELECT l FROM Land l WHERE l.status IN ('ACTIVE', 'APPROVED')")
    List<Land> findActiveLands();
}

