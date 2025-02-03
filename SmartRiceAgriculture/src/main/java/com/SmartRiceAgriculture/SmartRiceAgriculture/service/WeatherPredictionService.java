package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.config.MLConfig;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.WeatherPrediction;
import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.WeatherPredictionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherPredictionService {
    private final WeatherPredictionRepository weatherPredictionRepository;
    private final MLConfig mlConfig;
    private final ObjectMapper objectMapper;

    private static final int DAYS_TO_PREDICT = 7;
    private static final String PYTHON_EXECUTABLE = "C:\\projects\\SmartRiceAgriculture\\SmartRiceAgriculture\\src\\main\\resources\\ML\\ml_venv\\Scripts\\python.exe";
    private static final String PYTHON_SCRIPT = "C:\\projects\\SmartRiceAgriculture\\SmartRiceAgriculture\\src\\main\\resources\\ML\\ml_venv\\weather_predict.py";

    @Scheduled(cron = "0 0 0 * * *") // Run at midnight every day
    @Transactional
    public synchronized void generateDailyPredictions() {
        log.info("Starting daily weather prediction generation...");

        try {
            clearOldPredictions();

            if (!mlConfig.isMLEnabled()) {
                log.warn("ML is disabled. Using fallback weather predictions.");
                generateFallbackPredictions(DAYS_TO_PREDICT);
                return;
            }

            List<WeatherPrediction> predictions = generatePredictionsUsingML();
            if (!predictions.isEmpty()) {
                weatherPredictionRepository.saveAll(predictions);
                log.info("Saved {} daily predictions to the database.", predictions.size());
            } else {
                log.warn("ML script returned no predictions. Falling back to default predictions.");
                generateFallbackPredictions(DAYS_TO_PREDICT);
            }
        } catch (Exception e) {
            log.error("Error generating predictions: {}", e.getMessage(), e);
            generateFallbackPredictions(DAYS_TO_PREDICT);
        }
    }

    private void clearOldPredictions() {
        LocalDateTime now = LocalDateTime.now();
        weatherPredictionRepository.deleteByPredictionDateBefore(now);
        log.info("Cleared old predictions before {}", now);
    }

    private List<WeatherPrediction> generatePredictionsUsingML() {
        List<WeatherPrediction> predictions = new ArrayList<>();

        try {
            Process process = new ProcessBuilder(PYTHON_EXECUTABLE, PYTHON_SCRIPT).start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            boolean jsonStarted = false;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("[")) {
                    jsonStarted = true;
                }
                if (jsonStarted) {
                    output.append(line);
                }
            }

            String jsonOutput = output.toString();
            if (jsonOutput.isEmpty()) {
                log.warn("No output from Python script");
                return predictions;
            }

            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            List<Map<String, Object>> rawPredictions = objectMapper.readValue(
                    jsonOutput,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
            );

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (Map<String, Object> rawPrediction : rawPredictions) {
                WeatherPrediction prediction = new WeatherPrediction();
                prediction.setLocationId(Integer.valueOf(rawPrediction.get("location_id").toString()));
                prediction.setPredictionDate(LocalDateTime.parse(
                        rawPrediction.get("date").toString(),
                        formatter
                ));
                prediction.setTemperature(Double.valueOf(rawPrediction.get("temperature").toString()));
                prediction.setRainfall(Double.valueOf(rawPrediction.get("rainfall_probability").toString()) * 10);
                prediction.setRainfallProbability(Double.valueOf(rawPrediction.get("rainfall_probability").toString()));
                prediction.setWindSpeed(Double.valueOf(rawPrediction.get("wind_speed").toString()));
                prediction.setWeatherType(WeatherPrediction.WeatherType.valueOf(rawPrediction.get("weather_type").toString()));
                prediction.setIsFallback(false);

                predictions.add(prediction);
            }

            log.info("Successfully parsed {} predictions from Python output", predictions.size());
        } catch (Exception e) {
            log.error("Error running Python script for ML predictions: {}", e.getMessage(), e);
        }

        return predictions;
    }

    private List<WeatherPrediction> generateFallbackPredictions(int days) {
        log.info("Generating {} days of fallback predictions...", days);
        List<WeatherPrediction> fallbackPredictions = new ArrayList<>();
        LocalDateTime currentDate = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        // Generate predictions for each location
        for (int locationId = 0; locationId <= 26; locationId++) {
            for (int i = 0; i < days; i++) {
                WeatherPrediction prediction = new WeatherPrediction();
                prediction.setLocationId(locationId);
                prediction.setPredictionDate(currentDate.plusDays(i));
                prediction.setTemperature(25.0 + Math.random() * 5);
                prediction.setRainfall(Math.random() * 10);
                prediction.setRainfallProbability(Math.random());
                prediction.setWindSpeed(5.0 + Math.random() * 5);

                double rand = Math.random();
                if (rand < 0.2) prediction.setWeatherType(WeatherPrediction.WeatherType.SUNNY);
                else if (rand < 0.4) prediction.setWeatherType(WeatherPrediction.WeatherType.FAIR);
                else if (rand < 0.6) prediction.setWeatherType(WeatherPrediction.WeatherType.LIGHT_RAIN);
                else if (rand < 0.8) prediction.setWeatherType(WeatherPrediction.WeatherType.MODERATE_RAIN);
                else prediction.setWeatherType(WeatherPrediction.WeatherType.HEAVY_RAIN);

                prediction.setIsFallback(true);
                fallbackPredictions.add(prediction);
            }
        }

        List<WeatherPrediction> savedPredictions = weatherPredictionRepository.saveAll(fallbackPredictions);
        log.info("Saved {} fallback predictions", savedPredictions.size());
        return savedPredictions;
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableLocations() {
        LocalDateTime now = LocalDateTime.now();
        List<Integer> locations = weatherPredictionRepository.findDistinctLocationIds(now);

        if (locations.isEmpty()) {
            log.info("No locations found with predictions, generating new predictions...");
            generateDailyPredictions();
            locations = weatherPredictionRepository.findDistinctLocationIds(now);
        }

        return locations;
    }

    @Transactional(readOnly = true)
    public List<WeatherPrediction> getDailyPredictions(Integer locationId) {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.plusDays(1);
        log.info("Fetching daily predictions for location {} between {} and {}", locationId, now, endOfDay);

        List<WeatherPrediction> predictions = weatherPredictionRepository
                .findByLocationIdAndPredictionDateBetweenOrderByPredictionDateAsc(locationId, now, endOfDay);

        if (predictions.isEmpty()) {
            log.info("No daily predictions found for location {}, generating new predictions", locationId);
            synchronized (this) {
                generateDailyPredictions();
                predictions = weatherPredictionRepository
                        .findByLocationIdAndPredictionDateBetweenOrderByPredictionDateAsc(locationId, now, endOfDay);
            }
        }

        return predictions;
    }

    @Transactional(readOnly = true)
    public List<WeatherPrediction> getWeeklyPredictions(Integer locationId) {
        LocalDateTime now = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfWeek = now.plusDays(DAYS_TO_PREDICT);
        log.info("Fetching weekly predictions for location {} between {} and {}", locationId, now, endOfWeek);

        List<WeatherPrediction> predictions = weatherPredictionRepository
                .findByLocationIdAndPredictionDateBetweenOrderByPredictionDateAsc(locationId, now, endOfWeek);

        if (predictions.isEmpty() || predictions.size() < DAYS_TO_PREDICT) {
            log.info("Insufficient predictions found for location {}, generating new predictions", locationId);
            synchronized (this) {
                // Check again in case another thread just generated predictions
                predictions = weatherPredictionRepository
                        .findByLocationIdAndPredictionDateBetweenOrderByPredictionDateAsc(locationId, now, endOfWeek);
                if (predictions.isEmpty() || predictions.size() < DAYS_TO_PREDICT) {
                    generateDailyPredictions();
                    predictions = weatherPredictionRepository
                            .findByLocationIdAndPredictionDateBetweenOrderByPredictionDateAsc(locationId, now, endOfWeek);
                }
            }
        }

        // Ensure we have exactly 7 days of predictions
        if (predictions.size() < DAYS_TO_PREDICT) {
            log.warn("Still missing predictions after generation attempt. Falling back to default predictions");
            List<WeatherPrediction> fallbackPredictions = generateFallbackPredictions(DAYS_TO_PREDICT);
            predictions = fallbackPredictions.stream()
                    .filter(p -> p.getLocationId().equals(locationId))
                    .collect(Collectors.toList());
        }

        return predictions;
    }
}