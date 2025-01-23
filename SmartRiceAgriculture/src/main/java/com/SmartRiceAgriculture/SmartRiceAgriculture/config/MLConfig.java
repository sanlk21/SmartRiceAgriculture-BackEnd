package com.SmartRiceAgriculture.SmartRiceAgriculture.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import jakarta.annotation.PostConstruct;

import java.io.IOException;

@Configuration
@Getter
@Slf4j
public class MLConfig {
    private static final String MODEL_PATH = "ml/weather_prediction_model.keras";
    private static final String SCALER_PATH = "ml/weather_scalers.pkl";

    private ClassPathResource modelResource;
    private ClassPathResource scalerResource;

    @PostConstruct
    public void init() {
        try {
            modelResource = new ClassPathResource(MODEL_PATH);
            scalerResource = new ClassPathResource(SCALER_PATH);

            validateResources();
        } catch (Exception e) {
            log.error("Failed to initialize ML resources: {}", e.getMessage());
            throw new RuntimeException("ML configuration failed", e);
        }
    }

    private void validateResources() {
        if (!modelResource.exists()) {
            throw new IllegalStateException("Model file not found: " + MODEL_PATH);
        }
        if (!scalerResource.exists()) {
            throw new IllegalStateException("Scaler file not found: " + SCALER_PATH);
        }
    }

    public String getModelPath() {
        try {
            return modelResource.getURL().getPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get model path", e);
        }
    }

    public String getScalerPath() {
        try {
            return scalerResource.getURL().getPath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get scaler path", e);
        }
    }
}