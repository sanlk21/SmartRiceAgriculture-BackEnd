package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.DTO.WeatherPredictionDTO;
import com.SmartRiceAgriculture.SmartRiceAgriculture.service.WeatherPredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherPredictionController {
    private final WeatherPredictionService weatherPredictionService;

    @GetMapping("/daily")
    public ResponseEntity<List<WeatherPredictionDTO>> getDailyPredictions() {
        List<WeatherPredictionDTO> predictions = weatherPredictionService.getDailyPredictions()
                .stream()
                .map(WeatherPredictionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(predictions);
    }

    @GetMapping("/weekly")
    public ResponseEntity<List<WeatherPredictionDTO>> getWeeklyPredictions() {
        List<WeatherPredictionDTO> predictions = weatherPredictionService.getWeeklyPredictions()
                .stream()
                .map(WeatherPredictionDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(predictions);
    }
}
