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

    @Column(nullable = false)
    private String farmerNic;

    @Column(nullable = false)
    private Float quantity;

    @Column(nullable = false)
    private Float minimumPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiceVariety riceVariety;

    @Column(nullable = false)
    private LocalDateTime postedDate;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private LocalDateTime harvestDate;

    private String description;
    private String location;

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
    @PreUpdate
    protected void validateAndInitialize() {
        validateHarvestDate();
        initializeDates();
    }

    private void validateHarvestDate() {
        if (harvestDate == null) {
            throw new IllegalArgumentException("Harvest date must be specified");
        }

        if (harvestDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Harvest date must be in the future");
        }
    }

    private void initializeDates() {
        LocalDateTime now = LocalDateTime.now();

        if (postedDate == null) {
            postedDate = now;
        }

        if (expiryDate == null) {
            expiryDate = postedDate.plusDays(7);
        }
    }

    public void setStatus(BidStatus newStatus) {
        LocalDateTime currentHarvestDate = this.harvestDate;
        this.status = newStatus;
        this.harvestDate = currentHarvestDate;
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
        ACCEPTED,    // Bid has been accepted by farmer
        COMPLETED,   // Successful bid and transaction completed
        CANCELLED   // Cancelled by farmer or admin
    }

    // Helper methods to ensure harvest date is preserved
    public void setHarvestDate(LocalDateTime harvestDate) {
        this.harvestDate = harvestDate;
        validateHarvestDate();
    }

    public LocalDateTime getHarvestDate() {
        return this.harvestDate;
    }

    public void updateBidStatus(BidStatus newStatus) {
        LocalDateTime currentHarvestDate = this.harvestDate;
        this.status = newStatus;
        this.harvestDate = currentHarvestDate;
    }
}