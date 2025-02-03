package com.SmartRiceAgriculture.SmartRiceAgriculture.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "weather_predictions")
public class WeatherPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_id", nullable = false)
    private Integer locationId;

    @Column(name = "prediction_date")
    private LocalDateTime predictionDate;

    private Double temperature;
    private Double rainfall;

    @Column(name = "rainfall_probability")
    private Double rainfallProbability;

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_type")
    private WeatherType weatherType;

    @Column(name = "is_fallback")
    private Boolean isFallback = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum WeatherType {
        SUNNY("SUNNY"),
        FAIR("FAIR"),
        LIGHT_RAIN("LIGHT_RAIN"),
        MODERATE_RAIN("MODERATE_RAIN"),
        HEAVY_RAIN("HEAVY_RAIN");

        private final String value;

        WeatherType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}