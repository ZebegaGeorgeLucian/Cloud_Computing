package com.example.todo_app.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class BlobStorageService {
    private final BlobServiceClient blobServiceClient;

    @Autowired
    public BlobStorageService(KeyVaultService keyVaultService) {
        // Retrieve the SAS token from Key Vault
        String sasToken;
        try {
            sasToken = keyVaultService.getSecret("blob-storage-sas-token");
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve SAS token from Key Vault", e);
        }

        // Set your storage account name (ensure it matches your Azure Storage configuration)
        String accountName = "todostorage111";
        if (sasToken == null || sasToken.isEmpty()) {
            throw new IllegalArgumentException("SAS token cannot be null or empty");
        }

        // Ensure SAS token starts with "?"
        if (!sasToken.startsWith("?")) {
            sasToken = "?" + sasToken;
        }

        // Construct Blob Storage endpoint
        String endpoint = String.format("https://%s.blob.core.windows.net%s", accountName, sasToken);

        blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .buildClient();
    }

    /**
     * Uploads the given file to the specified container.
     *
     * @param containerName The name of the container (e.g., "task-files")
     * @param file          The file to upload.
     * @return The URL of the uploaded blob.
     */
    public String uploadFile(String containerName, MultipartFile file) {
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            if (!containerClient.exists()) {
                containerClient.create();
            }

            // Ensure a valid filename
            String blobName = file.getOriginalFilename();
            if (blobName == null || blobName.isEmpty()) {
                throw new IllegalArgumentException("File name cannot be null or empty");
            }

            BlobClient blobClient = containerClient.getBlobClient(blobName);
            blobClient.upload(file.getInputStream(), file.getSize(), true);
            return blobClient.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Blob Storage", e);
        }
    }
}