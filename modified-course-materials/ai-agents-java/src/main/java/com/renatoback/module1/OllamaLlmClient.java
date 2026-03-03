package com.renatoback.module1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * LLM client for a local Ollama server.
 *
 * Uses the /api/chat endpoint with {@code stream=false} so that
 * per-message roles are preserved.
 */
public class OllamaLlmClient implements LlmClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String model;

    public OllamaLlmClient(String baseUrl, String model) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = model;
    }

    @Override
    public String generateResponse(List<Message> messages) {
        String requestBody = buildRequestBody(messages);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/chat"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() / 100 != 2) {
                throw new RuntimeException("Ollama request failed with status " + response.statusCode()
                        + ": " + response.body());
            }

            return extractAssistantMessage(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Error calling Ollama at " + baseUrl, e);
        }
    }

    private String buildRequestBody(List<Message> messages) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("model", model);
            root.put("stream", false);

            ArrayNode msgArray = root.putArray("messages");
            for (Message message : messages) {
                ObjectNode m = msgArray.addObject();
                m.put("role", message.role().name().toLowerCase());
                m.put("content", message.content());
            }

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Ollama /api/chat request body", e);
        }
    }

    private String extractAssistantMessage(String body) {
        try {
            JsonNode root = MAPPER.readTree(body);
            JsonNode messageNode = root.path("message");
            JsonNode contentNode = messageNode.path("content");
            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                return contentNode.asText();
            }
            // Fallback if structure is different than expected
            return body;
        } catch (Exception e) {
            // If parsing fails, return raw JSON so caller can inspect it
            return body;
        }
    }

    private static String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }
}

