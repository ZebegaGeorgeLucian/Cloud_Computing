package com.example.hw3.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GcsService {

    private final Storage storage;
    private final VisionService visionService;
    private final DocumentAiService documentAiService;
    private final FirestoreService firestoreService; // Inject FirestoreService

    @Value("${gcs.bucket-name}")
    private String bucketName;

    public GcsService(Storage storage, VisionService visionService, DocumentAiService documentAiService, FirestoreService firestoreService) {
        this.storage = storage;
        this.visionService = visionService;
        this.documentAiService = documentAiService;
        this.firestoreService = firestoreService;
    }

    public String uploadAndProcessFile(MultipartFile file) throws IOException {
        // Upload the file to GCS
        String fileName = file.getOriginalFilename();
        String documentId = UUID.randomUUID().toString(); // Generate a unique ID for Firestore
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        Blob blob = storage.create(blobInfo, file.getBytes());
        String gcsUri = String.format("gs://%s/%s", bucketName, fileName);

        String extractedText = null;
        try {
            if (file.getContentType().equals("application/pdf")) {
                extractedText = documentAiService.extractTextFromPdf(gcsUri);
            } else if (file.getContentType().startsWith("image/")) {
                extractedText = visionService.extractText(gcsUri);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
            }

            System.out.println("Extracted Text: " + extractedText); // Log extracted text

            List<String> keywords = Arrays.stream(extractedText.toLowerCase().split("\\s+"))
                    .filter(word -> word.length() > 2)
                    .distinct()
                    .collect(Collectors.toList());

            // Save metadata to Firestore
            firestoreService.saveDocument(documentId, gcsUri, file.getContentType(), extractedText, keywords);
            System.out.println("Metadata saved to Firestore for document ID: " + documentId);

        } catch (Exception e) {
            throw new IOException("Failed to process file: " + e.getMessage(), e);
        }

        return gcsUri;
    }
}