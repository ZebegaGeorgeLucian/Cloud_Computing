package com.example.HW2Application.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Value("classpath:${firebase.credentials.path}")
    private Resource serviceAccountFile;

    @PostConstruct
    public void initializeFirebaseApp() {
        try (InputStream serviceAccount = serviceAccountFile.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase App initialized successfully.");
            }
        } catch (IOException e) {
            System.err.println("Error initializing Firebase App: " + e.getMessage());
        }
    }
}