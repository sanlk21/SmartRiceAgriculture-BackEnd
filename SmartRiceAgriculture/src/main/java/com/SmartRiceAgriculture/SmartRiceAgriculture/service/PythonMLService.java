package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j  // Add logging
public class PythonMLService {
    private final String PYTHON_SCRIPT_PATH;

    public PythonMLService() {
        // Get absolute path of the script
        try {
            Resource resource = new ClassPathResource("ml/weather_predict.py");
            PYTHON_SCRIPT_PATH = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Could not locate Python script", e);
        }
    }

    public double[] predictWeather(double[] inputData) {
        try {
            String inputString = Arrays.toString(inputData);

            // Use Python executable path from environment or configuration
            String pythonPath = System.getenv().getOrDefault("PYTHON_PATH", "python3");

            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    PYTHON_SCRIPT_PATH,
                    inputString
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read output with timeout
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                // Wait for process to complete with timeout
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    throw new RuntimeException("Python script execution timed out");
                }

                if (process.exitValue() != 0) {
                    throw new RuntimeException("Python script failed with exit code: "
                            + process.exitValue());
                }

                return parseOutput(output.toString());
            }

        } catch (Exception e) {
            log.error("Weather prediction failed", e);
            throw new RuntimeException("Failed to make weather prediction", e);
        }
    }

    private double[] parseOutput(String output) {
        try {
            return Arrays.stream(output.trim()
                            .replace("[", "")
                            .replace("]", "")
                            .split(","))
                    .map(String::trim)
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Python output: " + output, e);
        }
    }
}