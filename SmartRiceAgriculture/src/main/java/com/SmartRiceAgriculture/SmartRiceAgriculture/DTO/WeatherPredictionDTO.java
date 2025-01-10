package com.SmartRiceAgriculture.SmartRiceAgriculture.DTO;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.WeatherPrediction;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WeatherPredictionDTO {
    private LocalDateTime predictionDate;
    private double temperature;
    private double rainfall;
    private double windSpeed;
    private String weatherType;

    public static WeatherPredictionDTO fromEntity(WeatherPrediction entity) {
        WeatherPredictionDTO dto = new WeatherPredictionDTO();
        dto.setPredictionDate(entity.getPredictionDate());
        dto.setTemperature(entity.getTemperature());
        dto.setRainfall(entity.getRainfall());
        dto.setWindSpeed(entity.getWindSpeed());
        dto.setWeatherType(entity.getWeatherType().toString());
        return dto;
    }
}