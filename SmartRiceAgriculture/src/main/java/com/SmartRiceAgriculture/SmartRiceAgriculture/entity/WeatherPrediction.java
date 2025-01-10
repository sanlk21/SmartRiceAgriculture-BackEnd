package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "weather_predictions")
public class WeatherPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Added @Id annotation

    private LocalDateTime predictionDate;

    @Column(nullable = false)
    private Double temperature;

    @Column(nullable = false)
    private Double rainfall;

    @Column(nullable = false)
    private Double windSpeed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeatherType weatherType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum WeatherType {
        SUNNY,
        LIGHT_RAIN,
        MODERATE_RAIN,
        HEAVY_RAIN
    }
}
