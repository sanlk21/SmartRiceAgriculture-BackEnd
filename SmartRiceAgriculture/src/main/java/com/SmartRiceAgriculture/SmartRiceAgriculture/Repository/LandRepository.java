package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Land;
import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LandRepository extends JpaRepository<Land, Long> {
    List<Land> findByFarmerNic(String farmerNic);
    List<Land> findByStatus(LandStatus status);
    List<Land> findByDistrict(String district);
}
