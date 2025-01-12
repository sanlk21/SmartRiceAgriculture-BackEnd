package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PythonMLService {
    private final ResourceLoader resourceLoader;

    @Value("${python.executable.path}")
    private String pythonPath;

    @Value("${python.model.directory}")
    private String modelDirectory;

    public PythonMLService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public double[] predictWeather(double[] inputData) {
        try {
            // Verify Python executable
            log.info("Using Python executable: {}", pythonPath);

            // Get script path
            Resource scriptResource = resourceLoader.getResource("classpath:" + modelDirectory + "/weather_predict.py");
            String scriptPath = scriptResource.getFile().getAbsolutePath();
            log.info("Script path: {}", scriptPath);

            // Verify model files exist
            Resource modelResource = resourceLoader.getResource("classpath:" + modelDirectory + "/weather_prediction_model.keras");
            Resource scalerResource = resourceLoader.getResource("classpath:" + modelDirectory + "/weather_scalers.pkl");

            log.info("Model exists: {}", modelResource.exists());
            log.info("Scaler exists: {}", scalerResource.exists());

            // Create process
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    scriptPath,
                    Arrays.toString(inputData)
            );

            // Set model directory in environment
            pb.environment().put("MODEL_DIR",
                    resourceLoader.getResource("classpath:" + modelDirectory).getFile().getAbsolutePath());
            log.info("MODEL_DIR: {}", pb.environment().get("MODEL_DIR"));

            pb.redirectErrorStream(true);

            // Start process
            log.info("Starting Python process...");
            Process process = pb.start();

            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Python output: {}", line);
                }
            }

            // Wait with timeout
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                throw new RuntimeException("Python script execution timed out");
            }

            if (process.exitValue() != 0) {
                throw new RuntimeException("Python script failed with exit code: " +
                        process.exitValue() + "\nOutput: " + output);
            }

            return parseOutput(output.toString());

        } catch (Exception e) {
            log.error("Weather prediction failed", e);
            throw new RuntimeException("Failed to make weather prediction: " + e.getMessage(), e);
        }
    }

    private double[] parseOutput(String output) {
        try {
            String cleaned = output.trim()
                    .lines()
                    .filter(line -> line.startsWith("[") && line.endsWith("]"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No valid prediction output found"));

            return Arrays.stream(cleaned
                            .replace("[", "")
                            .replace("]", "")
                            .split(","))
                    .map(String::trim)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (Exception e) {
            log.error("Failed to parse Python output: {}", output, e);
            throw new RuntimeException("Failed to parse Python output: " + output, e);
        }
    }
}