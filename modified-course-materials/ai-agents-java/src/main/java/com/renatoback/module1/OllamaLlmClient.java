package com.renatoback.module1;

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
 * This implementation is intentionally minimal and uses the /api/generate
 * endpoint with {@code stream=false} to receive a single JSON response.
 */
public class OllamaLlmClient implements LlmClient {

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
        StringBuilder promptBuilder = new StringBuilder();
        for (Message message : messages) {
            promptBuilder
                    .append(message.getRole())
                    .append(": ")
                    .append(message.getContent())
                    .append("\n");
        }

        String prompt = promptBuilder.toString();

        String requestBody = """
                {
                  "model": %s,
                  "prompt": %s,
                  "stream": false
                }
                """.formatted(toJsonString(model), toJsonString(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
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

            return extractResponseField(response.body());
        } catch (Exception e) {
            throw new RuntimeException("Error calling Ollama at " + baseUrl, e);
        }
    }

    private static String normalizeBaseUrl(String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    private static String toJsonString(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    /**
     * Very small JSON helper that extracts the "response" field from
     * Ollama's /api/generate JSON response. For example:
     *
     * {"model":"...","response":"Hello world","done":true,...}
     *
     * If the field cannot be found, the raw JSON is returned.
     */
    private static String extractResponseField(String json) {
        String key = "\"response\":\"";
        int start = json.indexOf(key);
        if (start < 0) {
            return json;
        }
        int i = start + key.length();
        StringBuilder result = new StringBuilder();
        boolean escape = false;
        while (i < json.length()) {
            char c = json.charAt(i++);
            if (escape) {
                // Handle a few common escape sequences
                switch (c) {
                    case 'n' -> result.append('\n');
                    case 'r' -> result.append('\r');
                    case 't' -> result.append('\t');
                    case '"' -> result.append('"');
                    case '\\' -> result.append('\\');
                    default -> result.append(c);
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}

