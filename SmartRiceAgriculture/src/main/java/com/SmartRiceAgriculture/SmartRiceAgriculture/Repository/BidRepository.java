package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BidRepository extends JpaRepository<Bid, Long> {

    @Query("SELECT b FROM Bid b WHERE b.farmerNic = :farmerNic ORDER BY b.postedDate DESC")
    List<Bid> findByFarmerNic(@Param("farmerNic") String farmerNic);

    List<Bid> findByStatus(Bid.BidStatus status);

    List<Bid> findByWinningBuyerNic(String buyerNic);
}