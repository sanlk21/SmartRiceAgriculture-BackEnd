package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "lands")
public class Land {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "farmer_nic", nullable = false)
    private User farmer;

    @Column(nullable = false)
    private Double size;  // in hectares

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String district;

    private String soilType;

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    // Current crop details
    @ManyToOne
    @JoinColumn(name = "rice_variety_id")
    private RiceVariety currentCrop;

    private Double currentCropArea;  // area allocated for current crop

    public enum Status {
        ACTIVE,
        INACTIVE
    }
}