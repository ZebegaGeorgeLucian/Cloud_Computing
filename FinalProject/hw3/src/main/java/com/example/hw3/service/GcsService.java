package com.example.hw3.service;

import com.google.cloud.storage.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class GcsService {

    private static final Logger logger = LoggerFactory.getLogger(GcsService.class);

    private final Storage storage;
    private final VisionService visionService;
    private final DocumentAiService documentAiService;
    private final FirestoreService firestoreService;
    private final NaturalLanguageApiService naturalLanguageApiService;
    private final ElasticsearchService elasticsearchService;

    @Value("${gcs.bucket-name}")
    private String bucketName;

    public GcsService(Storage storage,
                      VisionService visionService,
                      DocumentAiService documentAiService,
                      FirestoreService firestoreService,
                      NaturalLanguageApiService naturalLanguageApiService,
                      ElasticsearchService elasticsearchService) {
        this.storage = storage;
        this.visionService = visionService;
        this.documentAiService = documentAiService;
        this.firestoreService = firestoreService;
        this.naturalLanguageApiService = naturalLanguageApiService;
        this.elasticsearchService = elasticsearchService;
    }

    public String uploadAndProcessFile(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "untitled-" + UUID.randomUUID();
        // Ensure unique file names in GCS to prevent overwrites if desired
        String gcsObjectName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String documentId = UUID.randomUUID().toString();

        BlobId blobId = BlobId.of(bucketName, gcsObjectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        logger.info("Uploading file to GCS: gs://{}/{}", bucketName, gcsObjectName);
        Blob blob = storage.create(blobInfo, file.getBytes());
        String gcsUri = blob.getMediaLink();
        gcsUri = String.format("gs://%s/%s", bucketName, gcsObjectName);


        String extractedText = null;
        List<String> entitiesAsKeywords;

        try {
            if (file.getContentType() == null) {
                throw new IllegalArgumentException("File content type is missing.");
            }
            logger.info("Processing file type: {}", file.getContentType());

            if (file.getContentType().equals("application/pdf")) {
                extractedText = documentAiService.extractTextFromPdf(gcsUri);
            } else if (file.getContentType().startsWith("image/")) {
                extractedText = visionService.extractText(gcsUri);
            } else {
                logger.warn("Unsupported file type: {}", file.getContentType());
                throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
            }

            if (extractedText == null || extractedText.trim().isEmpty()) {
                logger.info("No text extracted from the document: {}", gcsUri);
                entitiesAsKeywords = List.of();
            } else {
                logger.info("Extracted Text (snippet): {}...", extractedText.substring(0, Math.min(extractedText.length(), 200)));
                // Assuming you want to process English text for now.
                // Pass null for languageCode to let NLP API auto-detect, or "en" if you are sure.
                entitiesAsKeywords = naturalLanguageApiService.extractRelevantEntities(extractedText); // Removed explicit languageCode for now, ensure NLP service handles it
                logger.info("Extracted Relevant Entities: {}", entitiesAsKeywords);
            }

            firestoreService.saveDocument(documentId, gcsUri, file.getContentType(), extractedText, entitiesAsKeywords);
            logger.info("Metadata saved to Firestore for document ID: {}", documentId);

            // Now, index the relevant data in Elasticsearch
            Map<String, Object> esDocumentData = new HashMap<>();
            esDocumentData.put("gcsUri", gcsUri); // Use a consistent field name like gcsUri
            esDocumentData.put("documentType", file.getContentType()); // Use a consistent field name
            if (extractedText != null) {
                esDocumentData.put("extractedText", extractedText);
            }
            if (entitiesAsKeywords != null && !entitiesAsKeywords.isEmpty()) {
                esDocumentData.put("keywordsNlp", entitiesAsKeywords); // Use a distinct name for NLP keywords
            }
            // Add any other fields you want to search or facet on:
            // esDocumentData.put("uploadTimestamp", System.currentTimeMillis());
            // esDocumentData.put("originalFileName", originalFileName);

            try {
                elasticsearchService.indexDocument(documentId, esDocumentData); // Use the same documentId as Firestore for easy correlation
                logger.info("Document data indexed in Elasticsearch for ID: {}", documentId);
            } catch (IOException e) {
                logger.error("Failed to index document {} in Elasticsearch. Firestore data was saved. Manual re-index might be needed.", documentId, e);
                // Decide on your error handling strategy here.
                // For now, we let the upload succeed if Firestore save was okay, but log ES error.
            }

        } catch (Exception e) {
            logger.error("Failed to process file: " + gcsUri, e);
            // Clean up GCS file if processing fails
            storage.delete(blobId);
            logger.info("Deleted GCS file due to processing error: {}", gcsUri);
            throw new IOException("Failed to process file: " + e.getMessage(), e);
        }

        return gcsUri;
    }

    /**
     * Generates a V4 signed URL for downloading an object.
     * @param bucketName The name of the bucket.
     * @param objectName The name of the object (file path in the bucket).
     * @param duration The duration for which the URL will be valid.
     * @param timeUnit The time unit for the duration.
     * @return The signed URL as a String.
     * @throws StorageException If there's an issue generating the URL.
     */
    public String generateV4GetSignedUrl(String bucketName, String objectName, long duration, TimeUnit timeUnit) throws StorageException {
        if (this.storage == null) {
            logger.error("Storage client is not initialized. Cannot generate signed URL.");
            throw new IllegalStateException("Storage client is not initialized.");
        }
        logger.debug("Generating V4 signed URL for gs://{}/{}", bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, objectName)).build();

        URL url = storage.signUrl(blobInfo, duration, timeUnit, Storage.SignUrlOption.withV4Signature());
        logger.debug("Generated signed URL: {}", url.toString());
        return url.toString();
    }
}