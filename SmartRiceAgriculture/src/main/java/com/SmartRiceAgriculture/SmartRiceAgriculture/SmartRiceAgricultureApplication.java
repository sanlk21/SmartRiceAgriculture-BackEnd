package com.SmartRiceAgriculture.SmartRiceAgriculture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.SmartRiceAgriculture.SmartRiceAgriculture.entity")
public class SmartRiceAgricultureApplication {
	public static void main(String[] args) {
		SpringApplication.run(SmartRiceAgricultureApplication.class, args);
	}
}