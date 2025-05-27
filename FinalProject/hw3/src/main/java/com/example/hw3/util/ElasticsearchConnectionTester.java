package com.example.hw3.util; // Or any appropriate package

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.cluster.HealthResponse;
import co.elastic.clients.transport.endpoints.BooleanResponse; // <<--- THIS IMPORT IS NEEDED
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class ElasticsearchConnectionTester {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConnectionTester.class);

    @Bean
    public CommandLineRunner testElasticsearchConnection(ElasticsearchClient esClient) {
        return args -> {
            logger.info("Attempting to connect to Elasticsearch and get cluster health...");
            try {
                // Option 1: Ping the cluster (simple true/false)
                logger.info("Pinging Elasticsearch cluster...");
                BooleanResponse pingResult = esClient.ping(); // esClient.ping() returns BooleanResponse
                if (pingResult.value()) { // Access the boolean result via .value()
                    logger.info("Successfully pinged Elasticsearch cluster!");
                } else {
                    logger.error("Failed to ping Elasticsearch cluster. Ping returned false (but no exception). This is unusual if ping succeeds without error.");
                }

                // Option 2: Get cluster health (more detailed)
                logger.info("Getting Elasticsearch cluster health...");
                HealthResponse healthResponse = esClient.cluster().health();
                logger.info("Elasticsearch Cluster Health:");
                logger.info("  Cluster Name: {}", healthResponse.clusterName());
                logger.info("  Status: {}", healthResponse.status());
                // ... (rest of your health logging lines) ...
                if (healthResponse.status() == co.elastic.clients.elasticsearch._types.HealthStatus.Green) {
                    logger.info("Elasticsearch cluster status is GREEN. All good!");
                } else {
                    logger.warn("Elasticsearch cluster status is {} (not Green). Check your ES cluster!", healthResponse.status());
                }


            } catch (Exception e) {
                logger.error("Failed to connect to Elasticsearch or perform initial check: {}", e.getMessage());
                logger.error("Elasticsearch Connection Exception Details:", e);
            }
        };
    }
}