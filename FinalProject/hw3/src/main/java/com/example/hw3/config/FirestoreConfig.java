package com.example.hw3.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FirestoreConfig {
    @Bean
    public Firestore firestore() {
        FirestoreOptions options = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId("rich-surge-455615-u1")
                .setDatabaseId("docdata")
                .build();
        Firestore db = options.getService();
        System.out.println("Firestore bean created: " + db);
        return db;
    }
}