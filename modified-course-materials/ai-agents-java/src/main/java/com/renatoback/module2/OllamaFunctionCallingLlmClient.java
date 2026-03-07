package com.renatoback.module2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.renatoback.core.LlmClient;
import com.renatoback.core.Message;
import com.renatoback.core.Prompt;
import com.renatoback.core.Tool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * LLM client for a local Ollama server with Prompt-based communication
 * (messages + optional tools / function calling).
 *
 * Uses the /api/chat endpoint with {@code stream=false}. When tools are
 * provided and the model invokes one, the response is a JSON string
 * {@code {"tool":"name","args":{...}}} compatible with
 * {@link OpenAiFunctionCallingLlmClient}.
 */
public class OllamaFunctionCallingLlmClient implements LlmClient<Prompt> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String model;

    public OllamaFunctionCallingLlmClient(String baseUrl, String model) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = model;
    }

    @Override
    public String generateResponse(Prompt prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("prompt must not be null");
        }

        String requestBody = buildRequestBody(prompt);

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

            return parseResponse(response.body(), prompt.tools());
        } catch (Exception e) {
            throw new RuntimeException("Error calling Ollama at " + baseUrl, e);
        }
    }

    private String buildRequestBody(Prompt prompt) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            root.put("model", model);
            root.put("stream", false);

            ArrayNode msgArray = root.putArray("messages");
            for (Message message : prompt.messages()) {
                ObjectNode m = msgArray.addObject();
                m.put("role", message.role().name().toLowerCase());
                m.put("content", message.content());
            }

            List<Tool> tools = prompt.tools();
            if (tools != null && !tools.isEmpty()) {
                ArrayNode toolsArray = root.putArray("tools");
                for (Tool tool : tools) {
                    ObjectNode toolNode = toolsArray.addObject();
                    toolNode.put("type", "function");
                    ObjectNode fn = toolNode.putObject("function");
                    fn.put("name", tool.toolName());
                    fn.put("description", tool.description());
                    fn.set("parameters", MAPPER.valueToTree(tool.parameters()));
                }
            }

            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Ollama /api/chat request body", e);
        }
    }

    private String parseResponse(String body, List<Tool> tools) {
        try {
            JsonNode root = MAPPER.readTree(body);
            JsonNode messageNode = root.path("message");

            JsonNode toolCallsNode = messageNode.path("tool_calls");
            if (tools != null && !tools.isEmpty()
                    && !toolCallsNode.isMissingNode()
                    && toolCallsNode.isArray()
                    && toolCallsNode.size() > 0) {
                JsonNode firstToolCall = toolCallsNode.get(0);
                JsonNode fnNode = firstToolCall.path("function");
                String toolName = fnNode.path("name").asText();
                JsonNode argsNode = fnNode.path("arguments");

                Map<String, Object> args = argsNode.isObject()
                        ? MAPPER.convertValue(argsNode, new TypeReference<Map<String, Object>>() {})
                        : Map.of();

                ObjectNode result = MAPPER.createObjectNode();
                result.put("tool", toolName);
                result.set("args", MAPPER.valueToTree(args));
                return MAPPER.writeValueAsString(result);
            }

            JsonNode contentNode = messageNode.path("content");
            if (!contentNode.isMissingNode() && !contentNode.isNull()) {
                return contentNode.asText();
            }
            return body;
        } catch (Exception e) {
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
