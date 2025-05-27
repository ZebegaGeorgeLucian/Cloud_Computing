package com.example.hw3.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;

@Configuration
public class ElasticsearchConfig {
    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConfig.class);

    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private int port;
    @Value("${elasticsearch.scheme}")
    private String scheme;

    // We will no longer use @Value for the API key directly from properties here,
    // as we'll fetch it programmatically.
    // You still need the secret name and project ID. These can come from properties or be hardcoded.

    @Value("${gcp.project.id}") // Assuming you have this in application.properties or app.yaml env
    private String gcpProjectId;

    private final String esApiKeySecretName = "elasticsearch-api-key"; // Your secret name
    private final String esApiKeySecretVersion = "latest"; // Or a specific version

    @Bean
    public ElasticsearchClient elasticsearchClient() throws IOException { // Add IOException
        String actualApiKey = null;
        String secretResourceName = String.format("projects/%s/secrets/%s/versions/%s",
                gcpProjectId, esApiKeySecretName, esApiKeySecretVersion);
        logger.info("Attempting to fetch Elasticsearch API key from Secret Manager: {}", secretResourceName);

        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            SecretVersionName secretVersionName = SecretVersionName.parse(secretResourceName);
            AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
            actualApiKey = response.getPayload().getData().toStringUtf8();
            logger.info("Successfully fetched Elasticsearch API key from Secret Manager.");
            // TEMPORARY DEBUG - DO NOT LOG THE KEY IN PRODUCTION
            // logger.warn("APP ENGINE DEBUG - Fetched API Key (programmatic): '{}'", actualApiKey);
        } catch (IOException e) {
            logger.error("Failed to fetch Elasticsearch API key from Secret Manager (path: {}): {}", secretResourceName, e.getMessage(), e);
            // Decide on failure strategy: throw exception to prevent app startup, or try to continue without ES?
            // For now, let the exception propagate so startup fails clearly if key is not available.
            throw e;
        }

        if (!StringUtils.hasText(actualApiKey)) {
            logger.error("Elasticsearch API Key fetched from Secret Manager is NULL or EMPTY.");
            throw new IllegalStateException("Elasticsearch API Key is missing after attempting to fetch from Secret Manager.");
        }

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme));
        builder.setDefaultHeaders(new Header[]{
                new BasicHeader("Authorization", "ApiKey " + actualApiKey)
        });

        if ("https".equalsIgnoreCase(scheme)) {
            try {
                final SSLContext sslContext = SSLContexts.createDefault();
                builder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setSSLContext(sslContext));
            } catch (Exception e) {
                logger.error("Failed to set default SSL context for Elasticsearch: {}", e.getMessage(), e);
            }
        }

        RestClient restClient = builder.build();
        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        return new ElasticsearchClient(transport);
    }
}