package com.example.hw3;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PropertyTest {

    @Value("${spring.cloud.gcp.credentials.location}")
    private String credentialsLocation;

    @Value("${spring.cloud.gcp.project-id}")
    private String projectId;

    @PostConstruct
    public void logProperties() {
        System.out.println("Credentials location: " + credentialsLocation);
        System.out.println("Project ID: " + projectId);
    }
}