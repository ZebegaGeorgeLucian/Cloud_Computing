package com.example.hw3.controller;

import com.example.hw3.service.ElasticsearchService;
import com.example.hw3.service.FirestoreService;
import com.example.hw3.service.GcsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

    private final GcsService gcsService;
    private final FirestoreService firestoreService; // Inject FirestoreService
    private final ElasticsearchService elasticsearchService;

    // Update constructor to inject FirestoreService
    public DocumentController(GcsService gcsService,
                              FirestoreService firestoreService,
                              ElasticsearchService elasticsearchService) { // Add ElasticsearchService
        this.gcsService = gcsService;
        this.firestoreService = firestoreService;
        this.elasticsearchService = elasticsearchService; // Initialize
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        System.out.println("Incoming Request Content-Type: " + request.getContentType());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty or missing");
        }
        try {
            String gcsUri = gcsService.uploadAndProcessFile(file); // Use the processing method
            return ResponseEntity.ok("File uploaded and processed successfully. GCS URI: " + gcsUri);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error while uploading and processing file: " + e.getMessage());
        }
    }


    /**
     * Searches documents by a keyword.
     * This currently uses Firestore's array-contains query.
     *
     * @return A list of documents matching the keyword.
     */
    @GetMapping("/search")
    public ResponseEntity<ElasticsearchService.ExtendedSearchResponse> searchDocumentsAdvanced(
            @RequestParam String query,
            @RequestParam(required = false) List<String> queryFields,
            // Let's try to be more specific or cleanse the map
            @RequestParam(required = false) Map<String, String> allRequestParams
    ) {
        Map<String, String> actualFilters = new HashMap<>();
        if (allRequestParams != null) {
            for (Map.Entry<String, String> entry : allRequestParams.entrySet()) {
                if (!entry.getKey().equals("query") && !entry.getKey().equals("queryFields")) {
                    actualFilters.put(entry.getKey(), entry.getValue());
                }
            }
        }

        logger.info("Received ES advanced search request. Query: '{}', QueryFields: {}, ActualFilters: {}", query, queryFields, actualFilters);
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Search attempt with empty query.");
            return ResponseEntity.badRequest().build();
        }

        try {
            ElasticsearchService.ExtendedSearchResponse esResponse =
                    elasticsearchService.searchDocumentsAdvanced(query, actualFilters, queryFields); // Pass actualFilters

            if (esResponse.getHits() == null || esResponse.getHits().isEmpty()) {
                logger.info("No documents found in ES for advanced search. Query: {}, Filters: {}", query, actualFilters);
                return ResponseEntity.noContent().build();
            }

            // Process hits to add download URLs
            for (ElasticsearchService.ExtendedSearchHit hit : esResponse.getHits()) {
                if (hit.source != null && hit.source.get("gcsUri") instanceof String) {
                    String gcsUri = (String) hit.source.get("gcsUri");
                    if (gcsUri.startsWith("gs://")) {
                        String tempUri = gcsUri.substring("gs://".length());
                        int firstSlash = tempUri.indexOf('/');
                        if (firstSlash > 0 && firstSlash < tempUri.length() -1) {
                            String bucketName = tempUri.substring(0, firstSlash);
                            String objectName = tempUri.substring(firstSlash + 1);
                            try {
                                String downloadUrl = gcsService.generateV4GetSignedUrl(bucketName, objectName, 15, TimeUnit.MINUTES);
                                hit.source.put("downloadUrl", downloadUrl);
                            } catch (Exception e) {
                                logger.error("Error generating signed URL for GCS URI from ES result: " + gcsUri, e);
                                hit.source.put("downloadUrl", "Error: Could not generate link");
                            }
                        }
                    }
                }
            }
            return ResponseEntity.ok(esResponse);

        } catch (IOException e) {
            logger.error("Error during Elasticsearch advanced search. Query: " + query + ", Filters: " + actualFilters, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}