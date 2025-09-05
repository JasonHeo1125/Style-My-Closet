package com.stylemycloset.recommendation.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stylemycloset.recommendation.config.HuggingFaceProperties;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HuggingFaceClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final HuggingFaceProperties props;

    public HuggingFaceClient(HuggingFaceProperties props) {
        this.props = props;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> zeroShotClassify(String model, String input, List<String> candidateLabels) {
        try {
            Map<String, Object> request = Map.of(
                "inputs", input,
                "parameters", Map.of("candidate_labels", candidateLabels)
            );

            String body = objectMapper.writeValueAsString(request);
            String url = props.getBaseUrl().replaceAll("/$", "") + "/models/" + model;

            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + (props.getToken() == null ? "" : props.getToken()))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {});
            }
            throw new RuntimeException("HuggingFace API error: status=" + response.statusCode() + ", body=" + response.body());
        } catch (Exception e) {
            throw new RuntimeException("Failed to call HuggingFace API", e);
        }
    }
}


