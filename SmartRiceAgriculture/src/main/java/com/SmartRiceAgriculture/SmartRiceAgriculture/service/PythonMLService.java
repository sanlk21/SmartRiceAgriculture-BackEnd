package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
@Slf4j
public class PythonMLService {
    private final String PYTHON_SCRIPT_PATH;
    private final String PYTHON_EXECUTABLE;

    public PythonMLService() {
        // Get the Python executable path from environment or use default
        PYTHON_EXECUTABLE = System.getenv().getOrDefault("PYTHON_PATH", "python3");

        // Get absolute path of the script
        try {
            Resource resource = new ClassPathResource("ML/weather_predict.py");
            PYTHON_SCRIPT_PATH = resource.getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Could not locate Python script", e);
        }
    }

    public double[] predictWeather(double[] inputData) {
        try {
            // Convert input array to JSON string
            String inputString = new ObjectMapper().writeValueAsString(inputData);

            ProcessBuilder pb = new ProcessBuilder(
                    PYTHON_EXECUTABLE,
                    PYTHON_SCRIPT_PATH,
                    inputString
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }

                // Wait for process with timeout
                if (!process.waitFor(30, TimeUnit.SECONDS)) {
                    process.destroyForcibly();
                    throw new RuntimeException("Python script execution timed out");
                }

                if (process.exitValue() != 0) {
                    throw new RuntimeException("Python script failed with exit code: "
                            + process.exitValue());
                }

                // Parse output
                return new ObjectMapper().readValue(
                        output.toString(),
                        double[].class
                );
            }
        } catch (Exception e) {
            log.error("Weather prediction failed", e);
            throw new RuntimeException("Failed to make weather prediction", e);
        }
    }
}