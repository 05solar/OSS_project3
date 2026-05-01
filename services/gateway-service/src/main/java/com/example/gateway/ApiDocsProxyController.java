package com.example.gateway;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
public class ApiDocsProxyController {
    private final RestClient restClient;
    private final Map<String, String> docsTargets;

    public ApiDocsProxyController(
            RestClient restClient,
            @Value("${app.auth-base-url}") String authBaseUrl,
            @Value("${app.menu-base-url}") String menuBaseUrl,
            @Value("${app.order-base-url}") String orderBaseUrl,
            @Value("${app.review-base-url}") String reviewBaseUrl,
            @Value("${app.ai-recommendation-base-url}") String aiRecommendationBaseUrl,
            @Value("${app.ai-review-writer-base-url}") String aiReviewWriterBaseUrl,
            @Value("${app.ai-operations-base-url}") String aiOperationsBaseUrl) {
        this.restClient = restClient;
        this.docsTargets = Map.of(
                "auth", authBaseUrl + "/v3/api-docs",
                "menu", menuBaseUrl + "/v3/api-docs",
                "order", orderBaseUrl + "/v3/api-docs",
                "review", reviewBaseUrl + "/v3/api-docs",
                "ai-recommendation", aiRecommendationBaseUrl + "/openapi.json",
                "ai-review-writer", aiReviewWriterBaseUrl + "/openapi.json",
                "ai-operations", aiOperationsBaseUrl + "/openapi.json"
        );
    }

    @GetMapping(value = "/api-docs/{service}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> proxyDocs(@PathVariable String service) {
        String target = docsTargets.get(service);
        if (target == null) {
            throw new ResponseStatusException(BAD_REQUEST, "unknown service for api docs");
        }
        String body = restClient.get().uri(target).retrieve().body(String.class);
        return ResponseEntity.ok(body);
    }
}
