package com.SmartRiceAgriculture.SmartRiceAgriculture.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import lombok.Getter;
import java.io.File;
import java.io.IOException;

@Configuration
@Getter
public class MLConfig {
    private final File modelFile;
    private final File scalerFile;

    public MLConfig() throws IOException {
        this.modelFile = new ClassPathResource("ml/weather_prediction_model.keras").getFile();
        this.scalerFile = new ClassPathResource("ml/weather_scalers.pkl").getFile();
    }
}