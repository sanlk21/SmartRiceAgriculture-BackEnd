package com.SmartRiceAgriculture.SmartRiceAgriculture.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Getter
@Slf4j
public class MLConfig {
    private static final String ML_ROOT_DIR = "src/main/resources/ML";
    private static final String MODEL_PATH = "weather_prediction_model.keras";
    private static final String SCALER_PATH = "weather_scalers.pkl";
    private static final String VENV_DIR = "ml_venv";

    private Resource modelResource;
    private Resource scalerResource;
    private boolean mlEnabled = false;

    @PostConstruct
    public void init() {
        try {
            setupMLEnvironment();
        } catch (Exception e) {
            log.warn("ML environment setup failed: {}. Weather prediction will use fallback mode.", e.getMessage());
            mlEnabled = false;
        }
    }

    private void setupMLEnvironment() throws Exception {
        // Create necessary directories
        Path mlRootPath = Paths.get(ML_ROOT_DIR);
        Path venvPath = mlRootPath.resolve(VENV_DIR);
        Path modelPath = venvPath.resolve(MODEL_PATH);
        Path scalerPath = venvPath.resolve(SCALER_PATH);

        // Create directories if they don't exist
        Files.createDirectories(venvPath);

        // Set resource paths
        modelResource = new FileSystemResource(modelPath.toFile());
        scalerResource = new FileSystemResource(scalerPath.toFile());

        // Check if ML files exist
        if (!modelResource.exists() || !scalerResource.exists()) {
            log.warn("ML model files not found. Ensure the following files exist:");
            log.warn("- {}", modelPath.toAbsolutePath());
            log.warn("- {}", scalerPath.toAbsolutePath());
            return;
        }

        mlEnabled = true;
        log.info("ML environment setup completed successfully");
    }

    public String getModelPath() {
        return modelResource.getFilename();
    }

    public String getScalerPath() {
        return scalerResource.getFilename();
    }

    public boolean isMLEnabled() {
        return mlEnabled;
    }
}