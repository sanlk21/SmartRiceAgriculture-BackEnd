package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "fertilizer_quotas")
public class FertilizerQuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "farmer_nic", nullable = false)
    private User farmer;

    private Double allocatedAmount; // in kilograms

    private LocalDate allocationDate;

    private LocalDate expiryDate;

    private Boolean isCollected;

    @ManyToOne
    @JoinColumn(name = "allocated_by")
    private User governmentOfficial;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Season season;

    @Enumerated(EnumType.STRING)
    private QuotaStatus status;

    private String remarks;

    public enum Season {
        YALA, MAHA
    }

    public enum QuotaStatus {
        PENDING, ALLOCATED, COLLECTED
    }
}