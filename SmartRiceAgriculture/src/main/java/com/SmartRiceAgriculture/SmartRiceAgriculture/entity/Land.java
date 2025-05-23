package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import com.SmartRiceAgriculture.SmartRiceAgriculture.enums.LandStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Land {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String farmerNic;

    @Column(nullable = false)
    private Float size;  // in hectares

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String district;

    // Document fields
    private String documentName;
    private String documentType;
    private String documentPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LandStatus status = LandStatus.PENDING; // Initial status is PENDING

    // Fertilizer quotas
    private Float nitrogenQuota;
    private Float phosphorusQuota;
    private Float potassiumQuota;
    private Float totalNpkQuota;

    @OneToMany(mappedBy = "land", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FertilizerAllocation> fertilizerAllocations = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calculateFertilizerQuotas() {
        this.nitrogenQuota = this.size * 75;    // 75 kg/ha
        this.phosphorusQuota = this.size * 35;  // 35 kg/ha
        this.potassiumQuota = this.size * 40;   // 40 kg/ha
        this.totalNpkQuota = this.size * 150;   // 150 kg/ha total
    }
}
