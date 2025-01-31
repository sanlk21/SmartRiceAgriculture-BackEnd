package com.SmartRiceAgriculture.SmartRiceAgriculture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import javax.sql.DataSource;

@Configuration
public class JpaConfiguration {
    private static LocalContainerEntityManagerFactoryBean emf;

    @Autowired
    public void setEntityManagerFactory(LocalContainerEntityManagerFactoryBean emf) {
        JpaConfiguration.emf = emf;
    }

    public static LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        return emf;
    }
}