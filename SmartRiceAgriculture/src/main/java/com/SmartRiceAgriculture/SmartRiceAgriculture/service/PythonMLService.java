package com.SmartRiceAgriculture.SmartRiceAgriculture.service;

import org.python.util.PythonInterpreter;
import org.springframework.stereotype.Service;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;

@Service
public class PythonMLService {
    private final PythonInterpreter interpreter;

    public PythonMLService() {
        interpreter = new PythonInterpreter();
        initializePythonScript();
    }

    private void initializePythonScript() {
        try {
            // Load Python script
            ClassPathResource resource = new ClassPathResource("ml/weather_predict.py");
            interpreter.execfile(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Python script", e);
        }
    }

    public double[] predictWeather(double[] inputData) {
        try {
            // Convert Java array to Python list
            interpreter.set("input_data", inputData);
            interpreter.exec("predictions = predict_weather(input_data)");

            // Get predictions back from Python
            return (double[]) interpreter.get("predictions", double[].class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to make weather prediction", e);
        }
    }
}
