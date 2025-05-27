package com.example.hw3;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.api.core.ApiFuture;
import java.util.HashMap;
import java.util.Map;

public class FirestoreMinimalWriteTest {
    public static void main(String[] args) {
        Firestore firestore = FirestoreOptions.getDefaultInstance().toBuilder()
                .setProjectId("rich-surge-455615-u1")
                .setDatabaseId("docdata")
                .build()
                .getService();

        Map<String, Object> data = new HashMap<>();
        data.put("uri", "gs://test-bucket/test-file.pdf");
        data.put("type", "application/pdf");
        data.put("extractedText", "This is a test.");
        data.put("keywords", java.util.List.of("test", "firestore"));
        data.put("timestamp", com.google.cloud.firestore.FieldValue.serverTimestamp());

        ApiFuture<WriteResult> future = firestore.collection("documents").document("test-doc").set(data);
        try {
            WriteResult result = future.get();
            System.out.println("Document written with update time: " + result.getUpdateTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}