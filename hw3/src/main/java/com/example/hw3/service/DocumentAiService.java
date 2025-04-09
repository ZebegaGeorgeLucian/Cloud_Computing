package com.example.hw3.service;

import com.google.cloud.documentai.v1.Document;
import com.google.cloud.documentai.v1.DocumentProcessorServiceClient;
import com.google.cloud.documentai.v1.DocumentProcessorServiceSettings;
import com.google.cloud.documentai.v1.ProcessorName;
import com.google.cloud.documentai.v1.RawDocument;
import com.google.cloud.documentai.v1.ProcessRequest;
import com.google.cloud.documentai.v1.ProcessResponse;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DocumentAiService {

    private final Storage storage;

    // Replace these with your actual project details
    private static final String PROJECT_ID = "rich-surge-455615-u1";
    private static final String LOCATION = "eu";
    private static final String PROCESSOR_ID = "11fbd8c612b78e9c";

    public DocumentAiService(Storage storage) {
        this.storage = storage;
    }

    public String extractTextFromPdf(String gcsUri) throws Exception {
        String[] parts = gcsUri.replace("gs://", "").split("/", 2);
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid GCS URI: " + gcsUri);
        }
        String bucketName = parts[0];
        String fileName = parts[1];

        // Retrieve the file content from GCS.
        Blob blob = storage.get(bucketName, fileName);
        if (blob == null || !blob.exists()) {
            throw new IOException("Could not find file: " + gcsUri);
        }
        byte[] content = blob.getContent();

        RawDocument rawDocument = RawDocument.newBuilder()
                .setContent(ByteString.copyFrom(content))
                .setMimeType("application/pdf")
                .build();

        ProcessorName processorName = ProcessorName.of(PROJECT_ID, LOCATION, PROCESSOR_ID);

        // Set up the Document AI client with the endpoint for EU.
        DocumentProcessorServiceSettings settings = DocumentProcessorServiceSettings.newBuilder()
                .setEndpoint("eu-documentai.googleapis.com:443")
                .build();

        try (DocumentProcessorServiceClient client = DocumentProcessorServiceClient.create(settings)) {
            // Build the ProcessRequest with the raw document.
            ProcessRequest request = ProcessRequest.newBuilder()
                    .setName(processorName.toString())
                    .setRawDocument(rawDocument)
                    .build();

            // Process the document.
            ProcessResponse response = client.processDocument(request);
            Document document = response.getDocument();
            return document.getText();
        }
    }
}