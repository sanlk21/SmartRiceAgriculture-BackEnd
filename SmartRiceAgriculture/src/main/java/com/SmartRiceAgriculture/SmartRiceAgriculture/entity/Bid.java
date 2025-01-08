package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "bids")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Farmer's listing details
    @Column(nullable = false)
    private String farmerNic;

    @Column(nullable = false)
    private Float quantity; // in kg

    @Column(nullable = false)
    private Float minimumPrice; // price per kg

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiceVariety riceVariety;

    @Column(nullable = false)
    private LocalDateTime postedDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    private String description;
    private String location;

    // Bidding details
    @ElementCollection
    @CollectionTable(
            name = "bid_offers",
            joinColumns = @JoinColumn(name = "bid_id")
    )
    private List<BidOffer> bidOffers = new ArrayList<>();

    private String winningBuyerNic;
    private Float winningBidAmount;
    private LocalDateTime winningBidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status = BidStatus.ACTIVE;

    @PrePersist
    protected void onCreate() {
        postedDate = LocalDateTime.now();
        expiryDate = postedDate.plusDays(7);
    }

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BidOffer {
        private String buyerNic;
        private Float bidAmount;
        private LocalDateTime bidDate;
    }

    public enum RiceVariety {
        SAMBA,
        KIRI_SAMBA,
        NADU,
        KEKULU,
        RED_SAMBA,
        RED_NADU,
        SUWANDEL,
        KALU_HEENATI,
        PACHCHAPERUMAL,
        MADATHAWALU,
        KURULUTHUDA,
        RATH_SUWANDEL,
        HETADA_WEE,
        GONABARU,
        MURUNGAKAYAN
    }

    public enum BidStatus {
        ACTIVE,      // Bid is open for buyers
        EXPIRED,     // 7 days passed without successful bid
        COMPLETED,   // Successful bid and transaction completed
        CANCELLED   // Cancelled by farmer or admin
    }
}
