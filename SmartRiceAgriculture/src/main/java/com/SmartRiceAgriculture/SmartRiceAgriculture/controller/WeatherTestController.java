package com.SmartRiceAgriculture.SmartRiceAgriculture.controller;

import com.SmartRiceAgriculture.SmartRiceAgriculture.service.PythonMLService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherTestController {
    private final PythonMLService pythonMLService;

    @GetMapping("/test")
    public ResponseEntity<?> testML() {
        try {
            // Test with sample data [temperature, wind_speed, rainfall]
            double[] inputData = {25.0, 10.0, 0.0};
            double[] prediction = pythonMLService.predictWeather(inputData);

            return ResponseEntity.ok(Map.of(
                    "input", inputData,
                    "prediction", prediction,
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", e.getMessage(),
                            "status", "failed"
                    ));
        }
    }
}
