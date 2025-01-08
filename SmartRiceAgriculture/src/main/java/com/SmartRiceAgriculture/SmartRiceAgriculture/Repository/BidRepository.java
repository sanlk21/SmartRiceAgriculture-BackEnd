package com.SmartRiceAgriculture.SmartRiceAgriculture.Repository;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByStatus(Bid.BidStatus status);

    List<Bid> findByFarmerNic(String farmerNic);

    @Query("SELECT b FROM Bid b WHERE b.status = 'ACTIVE' AND b.expiryDate <= :now")
    List<Bid> findExpiredBids(LocalDateTime now);

    @Query("SELECT b FROM Bid b WHERE b.status = 'ACTIVE' AND b.winningBuyerNic = :buyerNic")
    List<Bid> findActiveWinningBidsByBuyer(String buyerNic);

    List<Bid> findByWinningBuyerNic(String buyerNic);
}
