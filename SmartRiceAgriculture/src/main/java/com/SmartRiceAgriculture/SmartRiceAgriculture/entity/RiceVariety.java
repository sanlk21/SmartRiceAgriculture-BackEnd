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
@Table(name = "rice_varieties")
public class RiceVariety {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String varietyName;

    private String description;

    private Integer growingDuration; // in days

    private Double expectedYieldPerHectare;

    @ManyToOne
    @JoinColumn(name = "farmer_nic")
    private User farmer;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    public enum Grade {
        GRADE_A, GRADE_B, GRADE_C
    }
}