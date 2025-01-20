package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "fertilizer_allocations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FertilizerAllocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String farmerNic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "land_id", nullable = false)
    private Land land; // Establish relationship to Land entity

    @Column(nullable = false)
    private Float allocatedAmount;  // in kg

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CultivationSeason season;

    @Column(nullable = false)
    private Integer year;

    private LocalDateTime distributionDate;

    private String distributionLocation;

    private String referenceNumber;

    @Column(nullable = false)
    private Boolean isCollected = false;

    private LocalDateTime collectionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    public enum CultivationSeason {
        MAHA,    // October to March
        YALA     // April to September
    }

    public enum Status {
        PENDING,         // Allocation created but not ready for collection
        READY,          // Ready for farmer collection
        COLLECTED,      // Farmer has collected
        EXPIRED         // Farmer didn't collect within timeframe
    }
}

