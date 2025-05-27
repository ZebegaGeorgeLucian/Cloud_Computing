package com.example.hw3.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/documents/**")
                        .allowedOrigins("https://react-app-150477955319.europe-west3.run.app")
                        .allowedMethods("GET", "POST")
                        .allowCredentials(true);
            }
        };
    }
}
