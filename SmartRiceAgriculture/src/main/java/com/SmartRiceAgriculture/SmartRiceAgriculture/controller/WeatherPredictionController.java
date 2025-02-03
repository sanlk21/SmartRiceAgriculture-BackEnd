package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.WeatherPrediction;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.WeatherPredictionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Slf4j
public class WeatherPredictionController {
    private final WeatherPredictionService weatherPredictionService;
    private static final int REQUIRED_FORECAST_DAYS = 7;

    @GetMapping("/forecast/{locationId}")
    public ResponseEntity<List<WeatherPrediction>> getForecast(@PathVariable Integer locationId) {
        try {
            List<WeatherPrediction> predictions = weatherPredictionService.getWeeklyPredictions(locationId);
            if (predictions.size() < REQUIRED_FORECAST_DAYS) {
                log.warn("Incomplete forecast data for location {}. Got {} days instead of {}",
                        locationId, predictions.size(), REQUIRED_FORECAST_DAYS);
                weatherPredictionService.generateDailyPredictions();
                predictions = weatherPredictionService.getWeeklyPredictions(locationId);
            }
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            log.error("Error getting forecast for location {}: {}", locationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/daily/{locationId}")
    public ResponseEntity<List<WeatherPrediction>> getDailyForecast(@PathVariable Integer locationId) {
        try {
            List<WeatherPrediction> predictions = weatherPredictionService.getDailyPredictions(locationId);
            if (predictions.isEmpty()) {
                weatherPredictionService.generateDailyPredictions();
                predictions = weatherPredictionService.getDailyPredictions(locationId);
            }
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            log.error("Error getting daily forecast for location {}: {}", locationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/weekly/{locationId}")
    public ResponseEntity<List<WeatherPrediction>> getWeeklyForecast(@PathVariable Integer locationId) {
        try {
            List<WeatherPrediction> predictions = weatherPredictionService.getWeeklyPredictions(locationId);
            if (predictions.size() < REQUIRED_FORECAST_DAYS) {
                log.warn("Incomplete weekly forecast data for location {}. Got {} days instead of {}",
                        locationId, predictions.size(), REQUIRED_FORECAST_DAYS);
                weatherPredictionService.generateDailyPredictions();
                predictions = weatherPredictionService.getWeeklyPredictions(locationId);
            }
            return ResponseEntity.ok(predictions);
        } catch (Exception e) {
            log.error("Error getting weekly forecast for location {}: {}", locationId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generatePredictions() {
        try {
            log.info("Manually triggering weather prediction generation...");
            weatherPredictionService.generateDailyPredictions();
            return ResponseEntity.ok("Weather predictions generated successfully");
        } catch (Exception e) {
            log.error("Error generating predictions: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed to generate weather predictions: " + e.getMessage());
        }
    }

    @GetMapping("/locations")
    public ResponseEntity<List<Integer>> getAvailableLocations() {
        try {
            List<Integer> locations = weatherPredictionService.getAvailableLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("Error getting available locations: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/debug/{locationId}")
    public ResponseEntity<String> debugWeatherService(@PathVariable Integer locationId) {
        try {
            log.info("Running weather service diagnostics for location {}...", locationId);
            List<WeatherPrediction> predictions = weatherPredictionService.getWeeklyPredictions(locationId);
            return ResponseEntity.ok(String.format(
                    "Weather service is running. Current prediction count for location %d: %d/%d",
                    locationId, predictions.size(), REQUIRED_FORECAST_DAYS
            ));
        } catch (Exception e) {
            log.error("Error running diagnostics for location {}: {}", locationId, e.getMessage());
            return ResponseEntity.internalServerError().body("Diagnostics failed: " + e.getMessage());
        }
    }
}