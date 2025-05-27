package com.example.hw3.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;

import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ElasticsearchService {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchService.class);
    private final ElasticsearchClient esClient;
    public static final String INDEX_NAME = "search-doc";

    // ... (constructor, ensureIndexExistsWithMappings, indexDocument
    public ElasticsearchService(ElasticsearchClient esClient) {
        this.esClient = esClient;
        try {
            ensureIndexExistsWithMappings();
        } catch (IOException e) {
            logger.error("Failed to ensure Elasticsearch index '{}' exists with mappings on startup.", INDEX_NAME, e);
        }
    }

    private void ensureIndexExistsWithMappings() throws IOException {
        boolean exists = esClient.indices().exists(b -> b.index(INDEX_NAME)).value();
        if (!exists) {
            logger.info("Index '{}' does not exist. Creating it now with mappings.", INDEX_NAME);

            Map<String, Property> properties = new HashMap<>();
            properties.put("gcsUri", Property.of(p -> p.keyword(k -> k.index(true))));
            properties.put("documentType", Property.of(p -> p.keyword(k -> k.index(true))));
            properties.put("extractedText", Property.of(p -> p.text(t -> t.analyzer("standard"))));
            properties.put("keywordsNlp", Property.of(p -> p.keyword(k -> k.index(true))));
            properties.put("uploadTimestamp", Property.of(p -> p.date(d -> d)));
            properties.put("originalFileName", Property.of(p -> p.text(t -> t.analyzer("standard").fields("keyword", f -> f.keyword(k -> k.ignoreAbove(256))))));

            TypeMapping mapping = TypeMapping.of(tm -> tm.properties(properties));
            esClient.indices().create(c -> c.index(INDEX_NAME).mappings(mapping));
            logger.info("Index '{}' created successfully with mappings.", INDEX_NAME);
        } else {
            logger.info("Index '{}' already exists.", INDEX_NAME);
        }
    }
    public void indexDocument(String documentId, Map<String, Object> documentData) throws IOException {
        if (documentData.containsKey("uploadTimestamp") && documentData.get("uploadTimestamp") instanceof com.google.cloud.Timestamp) {
            com.google.cloud.Timestamp ts = (com.google.cloud.Timestamp) documentData.get("uploadTimestamp");
            documentData.put("uploadTimestamp", ts.toDate().toInstant().toEpochMilli());
        } else if (documentData.containsKey("uploadTimestamp") && documentData.get("uploadTimestamp") instanceof java.util.Date) {
            java.util.Date date = (java.util.Date) documentData.get("uploadTimestamp");
            documentData.put("uploadTimestamp", date.toInstant().toEpochMilli());
        }

        IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                .index(INDEX_NAME)
                .id(documentId)
                .document(documentData));
        esClient.index(request);
        logger.info("Document indexed successfully in Elasticsearch. ID: {}", documentId);
    }


    public static class FacetBucket {
        public String key;
        public long docCount;
        public FacetBucket(String key, long docCount) { this.key = key; this.docCount = docCount; }
        public String getKey() { return key; }
        public long getDocCount() { return docCount; }
    }

    public static class ExtendedSearchHit {
        public String id;
        public float score;
        public Map<String, Object> source;
        public Map<String, List<String>> highlight; // Using Map<String, List<String>> for highlight
        public ExtendedSearchHit(String id, float score, Map<String, Object> source, Map<String, List<String>> highlight) {
            this.id = id; this.score = score; this.source = source; this.highlight = highlight;
        }
        public String getId() { return id; }
        public float getScore() { return score; }
        public Map<String, Object> getSource() { return source; }
        public Map<String, List<String>> getHighlight() { return highlight; }
    }

    public static class ExtendedSearchResponse {
        public List<ExtendedSearchHit> hits;
        public Map<String, List<FacetBucket>> facets;
        public long totalHits;
        public ExtendedSearchResponse(List<ExtendedSearchHit> hits, Map<String, List<FacetBucket>> facets, long totalHits) {
            this.hits = hits; this.facets = facets; this.totalHits = totalHits;
        }
        public List<ExtendedSearchHit> getHits() { return hits; }
        public Map<String, List<FacetBucket>> getFacets() { return facets; }
        public long getTotalHits() { return totalHits; }
    }


    public ExtendedSearchResponse searchDocumentsAdvanced(String queryText,
                                                          Map<String, String> filters,
                                                          List<String> fieldsToSearchForQueryText) throws IOException {
        logger.info("Advanced ES search for query: '{}', filters: {}, mainQueryFields: {}", queryText, filters, fieldsToSearchForQueryText);

        List<String> defaultQueryFields = (fieldsToSearchForQueryText == null || fieldsToSearchForQueryText.isEmpty())
                ? List.of("extractedText", "keywordsNlp", "originalFileName")
                : fieldsToSearchForQueryText;

        Query mainQuery = Query.of(q -> q
                .multiMatch(m -> m
                        .query(queryText)
                        .fields(defaultQueryFields.stream().map(f -> {
                            if (f.equals("keywordsNlp")) return f + "^3"; // Boost keywordsNlp
                            if (f.equals("originalFileName")) return f + "^2"; // Boost originalFileName
                            return f;
                        }).collect(Collectors.toList()))
                        .fuzziness("AUTO")
                        .operator(co.elastic.clients.elasticsearch._types.query_dsl.Operator.And)
                        .minimumShouldMatch("75%")
                )
        );

        List<Query> filterQueries = new ArrayList<>();
        if (filters != null) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                filterQueries.add(Query.of(q -> q.term(t -> t.field(entry.getKey() + (entry.getKey().equals("originalFileName") ? ".keyword" : "")).value(entry.getValue()))));
            }
        }

        Query finalQuery = Query.of(q -> q
                .bool(b -> {
                    b.must(mainQuery);
                    if (!filterQueries.isEmpty()) {
                        b.filter(filterQueries);
                    }
                    return b;
                })
        );

        SearchRequest.Builder requestBuilder = new SearchRequest.Builder();
        requestBuilder.index(INDEX_NAME)
                .query(finalQuery)
                .size(20)
                .highlight(h -> h
                        .fields("extractedText", hf -> hf.preTags("<strong>").postTags("</strong>").numberOfFragments(3).fragmentSize(150))
                )
                // Define aggregations using the Aggregation.of builder
                .aggregations("documentType_facet", agg -> agg
                        .terms(t -> t.field("documentType").size(10)) // Terms aggregation on documentType
                )
                .aggregations("keywordsNlp_facet", agg -> agg
                        .terms(t -> t.field("keywordsNlp").size(20)) // Terms aggregation on keywordsNlp
                );

        SearchResponse<Map> response = esClient.search(requestBuilder.build(), Map.class);

        List<ExtendedSearchHit> processedHits = new ArrayList<>();
        if (response.hits().hits() != null) {
            for (Hit<Map> hit : response.hits().hits()) {
                Map<String, Object> source = hit.source() != null ? new HashMap<>(hit.source()) : new HashMap<>();
                // Extracting highlight
                Map<String, List<String>> highlightSnippets = hit.highlight() != null ? new HashMap<>(hit.highlight()) : new HashMap<>();

                processedHits.add(new ExtendedSearchHit(
                        hit.id(),
                        hit.score() != null ? hit.score().floatValue() : 0.0f,
                        source,
                        highlightSnippets // Pass the highlight map
                ));
            }
        }

        Map<String, List<FacetBucket>> facets = new HashMap<>();
        // Process documentType_facet aggregation
        Aggregate docTypeAgg = response.aggregations().get("documentType_facet");
        if (docTypeAgg != null && docTypeAgg.isSterms()) { // sterms for String Terms aggregation
            List<FacetBucket> docTypeBuckets = docTypeAgg.sterms().buckets().array().stream()
                    .map(bucket -> new FacetBucket(bucket.key().stringValue(), bucket.docCount()))
                    .collect(Collectors.toList());
            facets.put("documentType", docTypeBuckets);
        }

        // Process keywordsNlp_facet aggregation
        Aggregate keywordsAgg = response.aggregations().get("keywordsNlp_facet");
        if (keywordsAgg != null && keywordsAgg.isSterms()) {
            List<FacetBucket> keywordBuckets = keywordsAgg.sterms().buckets().array().stream()
                    .map(bucket -> new FacetBucket(bucket.key().stringValue(), bucket.docCount()))
                    .collect(Collectors.toList());
            facets.put("keywordsNlp", keywordBuckets);
        }

        long totalHitsValue = response.hits().total() != null ? response.hits().total().value() : 0;

        return new ExtendedSearchResponse(processedHits, facets, totalHitsValue);
    }
}