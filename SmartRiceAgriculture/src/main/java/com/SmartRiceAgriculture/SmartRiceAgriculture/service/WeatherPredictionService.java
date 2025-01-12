package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.WeatherPredictionRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.config.MLConfig;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.WeatherPrediction;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class WeatherPredictionService {
    private final WeatherPredictionRepository weatherPredictionRepository;
    private final MLConfig mlConfig;

    // Existing methods
    public List<WeatherPrediction> getDailyPredictions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = now.plusDays(1);
        return weatherPredictionRepository.findByPredictionDateBetweenOrderByPredictionDateAsc(now, endOfDay);
    }

    public List<WeatherPrediction> getWeeklyPredictions() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfWeek = now.plusDays(7);
        return weatherPredictionRepository.findByPredictionDateBetweenOrderByPredictionDateAsc(now, endOfWeek);
    }

    // New methods for ML predictions
    @Scheduled(cron = "0 0 0 * * *")  // Run at midnight every day
    public void generateDailyPredictions() {
        try {
            List<WeatherPrediction> predictions = generatePredictions(1);
            weatherPredictionRepository.saveAll(predictions);
        } catch (Exception e) {
            // Add proper error handling/logging
            System.err.println("Error generating daily predictions: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 * * MON")  // Run at midnight every Monday
    public void generateWeeklyPredictions() {
        try {
            List<WeatherPrediction> predictions = generatePredictions(7);
            weatherPredictionRepository.saveAll(predictions);
        } catch (Exception e) {
            System.err.println("Error generating weekly predictions: " + e.getMessage());
        }
    }

    private List<WeatherPrediction> generatePredictions(int days) {
        List<WeatherPrediction> predictions = new ArrayList<>();
        LocalDateTime currentDate = LocalDateTime.now();

        for (int i = 0; i < days; i++) {
            WeatherPrediction prediction = new WeatherPrediction();
            prediction.setPredictionDate(currentDate.plusDays(i));

            // Here we'll add the actual ML prediction logic
            // For now, adding placeholder logic
            setPredictionValues(prediction);

            predictions.add(prediction);
        }

        return predictions;
    }

    private void setPredictionValues(WeatherPrediction prediction) {
        // Using placeholder values
        prediction.setTemperature(25.0);
        prediction.setRainfall(0.0);
        prediction.setWindSpeed(10.0);
        prediction.setWeatherType(WeatherPrediction.WeatherType.SUNNY);
    }

    // Method to manually trigger predictions
    public List<WeatherPrediction> generateOnDemandPrediction() {
        List<WeatherPrediction> predictions = generatePredictions(1);
        return weatherPredictionRepository.saveAll(predictions);
    }
}