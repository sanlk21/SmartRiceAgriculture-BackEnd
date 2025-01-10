package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

@Service
public class PythonMLService {

    public double[] predictWeather(double[] inputData) {
        try {
            // Convert input data to JSON-like format for Python script
            String inputString = Arrays.toString(inputData);

            // Call the Python script using ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(
                    "python3", // Ensure Python 3 is installed and available in PATH
                    "src/main/resources/ml/weather_predict.py",
                    inputString
            );

            // Redirect errors
            pb.redirectErrorStream(true);

            // Start the process
            Process process = pb.start();

            // Read the output from the Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Parse the Python script output (expected to be a JSON-like array)
            String[] results = output.toString().replace("[", "").replace("]", "").split(",");
            return Arrays.stream(results).mapToDouble(Double::parseDouble).toArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to make weather prediction", e);
        }
    }
}
