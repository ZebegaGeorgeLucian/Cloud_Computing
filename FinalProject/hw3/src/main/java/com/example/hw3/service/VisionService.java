package com.example.hw3.service;

import com.google.cloud.vision.v1.*;
import org.springframework.stereotype.Service;

@Service
public class VisionService {

    public String extractText(String gcsUri) throws Exception {
        // Create a Vision client
        try (ImageAnnotatorClient visionClient = ImageAnnotatorClient.create()) {
            // Define the image source
            ImageSource imageSource = ImageSource.newBuilder().setGcsImageUri(gcsUri).build();
            Image image = Image.newBuilder().setSource(imageSource).build();

            // Create a request for text detection
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build())
                    .setImage(image)
                    .build();

            // Send the request and get the response
            BatchAnnotateImagesResponse response = visionClient.batchAnnotateImages(
                    java.util.Collections.singletonList(request));

            // Extract text from the response
            AnnotateImageResponse imageResponse = response.getResponsesList().get(0);
            if (imageResponse.hasError()) {
                throw new Exception("Error during text extraction: " + imageResponse.getError().getMessage());
            }

            return imageResponse.getFullTextAnnotation().getText();
        }
    }
}