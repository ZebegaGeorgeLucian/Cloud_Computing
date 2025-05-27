package com.example.hw3.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    private final Firestore firestore;

    public FirestoreService(Firestore firestore) {
        this.firestore = firestore;
        System.out.println("FirestoreService initialized with: " + firestore);
    }


    /**
     * Saves document metadata into the "documents" collection.
     * @param documentId Unique identifier for the document
     * @param uri GCS URI of the file
     * @param type MIME type of the file (e.g., "application/pdf")
     * @param extractedText The full extracted text
     * @param keywords List of keywords extracted from the text
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void saveDocument(String documentId, String uri, String type, String extractedText, List<String> keywords)
            throws ExecutionException, InterruptedException {
        Map<String, Object> documentData = new HashMap<>();
        documentData.put("uri", uri);
        documentData.put("type", type);
        documentData.put("extractedText", extractedText);
        documentData.put("keywords", keywords);
        documentData.put("timestamp", FieldValue.serverTimestamp());

        System.out.println("Document data to save: " + documentData);

        try {
            ApiFuture<WriteResult> future = firestore.collection("documents")
                    .document(documentId)
                    .set(documentData);
            WriteResult result = future.get();
            System.out.println("Document saved with update time: " + result.getUpdateTime());
        } catch (Exception e) {
            System.err.println("Error saving document: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * Queries documents that contain the given keyword.
     * @param keyword The search keyword. (Lowercase it for consistency.)
     * @return A list of documents (as maps of field names to values) matching the query.
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public List<Map<String, Object>> searchByKeyword(String keyword) throws ExecutionException, InterruptedException {
        // Assuming your keywords in Firestore are stored in lowercase.
        // If not, ensure the keyword here matches the case in Firestore or query on multiple cases.
        // The NLP service now adds prefixes like "price:", "date:", so direct keyword search might
        // need adjustment or you search on the raw entity name if also stored.
        // For now, this searches for an exact match in the 'keywords' array.
        Query query = firestore.collection("documents")
                .whereArrayContains("keywords", keyword.toLowerCase()); // Ensure keyword is lowercased if that's how they are stored

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Map<String, Object>> results = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            results.add(document.getData());
        }
        return results;
    }
}