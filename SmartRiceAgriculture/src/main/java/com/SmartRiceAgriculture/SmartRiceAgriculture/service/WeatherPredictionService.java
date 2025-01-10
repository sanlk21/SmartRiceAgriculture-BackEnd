package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.SmartRiceAgriculture.SmartRiceAgriculture.Repository.WeatherPredictionRepository;
import com.SmartRiceAgriculture.SmartRiceAgriculture.entity.WeatherPrediction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherPredictionService {
    private final WeatherPredictionRepository weatherPredictionRepository;

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
}
