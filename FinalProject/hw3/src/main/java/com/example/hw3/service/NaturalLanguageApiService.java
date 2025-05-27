package com.example.hw3.service;

import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.Document.Type;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings; // For endpoint configuration if needed
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NaturalLanguageApiService {
    private static final Logger logger = LoggerFactory.getLogger(NaturalLanguageApiService.class);

    // Define the entity types you are interested in
    private static final EnumSet<Entity.Type> TARGET_ENTITY_TYPES = EnumSet.of(
            Entity.Type.PERSON,           // Names of people
            Entity.Type.ORGANIZATION,     // Companies, institutions
            Entity.Type.LOCATION,         // Countries, cities, addresses
            Entity.Type.EVENT,            // Named events
            Entity.Type.WORK_OF_ART,      // Titles of books, songs
            Entity.Type.CONSUMER_GOOD,    // Products
            Entity.Type.NUMBER,           // Numerical values (can include quantities, not always monetary)
            Entity.Type.PRICE,            // Monetary values
            Entity.Type.DATE,              // Dates (can be specific or relative)
            Entity.Type.ADDRESS, // More specific location
            Entity.Type.PHONE_NUMBER
            // Add other types as needed from com.google.cloud.language.v1.Entity.Type
    );
    private static final float MIN_SALIENCE_THRESHOLD = 0.01f;
    public List<String> extractRelevantEntities(String text) throws IOException {
        // You might need to configure the endpoint if you are not using the default global one
        LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().setEndpoint("eu-language.googleapis.com:443").build();
        // try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
        List<String> relevantEntities;
        try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
            if (text == null || text.trim().isEmpty()) {
                return List.of(); // Return empty list if no text
            }

            Document doc = Document.newBuilder().setContent(text).setType(Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder()
                    .setDocument(doc)
                    .setEncodingType(EncodingType.UTF8)
                    .build();

            AnalyzeEntitiesResponse response = language.analyzeEntities(request);

            relevantEntities = new ArrayList<>();
            for (Entity entity : response.getEntitiesList()) {
                if (entity.getSalience() < MIN_SALIENCE_THRESHOLD) {
                    logger.debug("Skipping entity '{}' (type: {}) due to low salience: {}",
                            entity.getName(), entity.getType(), entity.getSalience());
                    continue; // Skip this entity if its salience is below the threshold
                }

                logger.info("Processing Entity: {}, Type: {}, Salience: {:.3f}, Metadata: {}",
                        entity.getName(), entity.getType(), entity.getSalience(), entity.getMetadataMap());

                String entityValue = entity.getName();

                if (entity.getType() == Entity.Type.PRICE) {
                    Map<String, String> metadata = entity.getMetadataMap();
                    String currency = metadata.get("currency"); // e.g., "USD", "EUR"
                    String value = metadata.get("value");       // e.g., "100.00", "50"

                    if (currency != null && !currency.isEmpty() && value != null && !value.isEmpty()) {
                        entityValue = String.format("price:%s %s", currency.toLowerCase(), value);
                    } else {
                        // Fallback if currency or value is missing in metadata, but it's a PRICE type
                        entityValue = "price:" + entity.getName().replaceAll("[^0-9.,]", ""); // Try to extract numeric part
                    }
                } else if (entity.getType() == Entity.Type.DATE) {
                    Map<String, String> metadata = entity.getMetadataMap();
                    String year = metadata.get("year");
                    String month = metadata.get("month");
                    String day = metadata.get("day");

                    // Construct a standard date format if all parts are available
                    if (year != null && !year.isEmpty() &&
                            month != null && !month.isEmpty() &&
                            day != null && !day.isEmpty()) {
                        try {
                            // Ensure month and day are two digits for consistent formatting
                            String formattedMonth = String.format("%02d", Integer.parseInt(month));
                            String formattedDay = String.format("%02d", Integer.parseInt(day));
                            entityValue = String.format("date:%s-%s-%s", year, formattedMonth, formattedDay);
                        } catch (NumberFormatException e) {
                            // If month/day are not numbers (e.g., "PRESENT_REF" for relative dates),
                            // fallback to the entity name or a more general representation.
                            System.err.println("Could not parse date parts for '" + entity.getName() + "': " + e.getMessage());
                            entityValue = "date:" + entity.getName(); // Fallback to original name with a prefix
                        }
                    } else {
                        // Fallback if essential date parts are missing,
                        // could be a relative date like "tomorrow" or "next week"
                        entityValue = "date:" + entity.getName();
                    }
                }

                relevantEntities.add(entityValue.toLowerCase());
            }
        }
        // Return distinct entities
        return relevantEntities.stream().distinct().collect(Collectors.toList());
    }
    }