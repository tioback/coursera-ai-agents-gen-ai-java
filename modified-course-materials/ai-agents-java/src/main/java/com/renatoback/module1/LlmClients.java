package com.renatoback.module1;

/**
 * Simple factory for selecting an {@link LlmClient} at runtime.
 *
 * Provider selection is controlled via environment variables:
 * - LLM_PROVIDER: openai (default), ollama, anthropic, gemini, ...
 *
 * Provider-specific configuration is intentionally minimal here and can be expanded
 * as you add more implementations.
 */
public final class LlmClients {

    private LlmClients() {
    }

    public static LlmClient fromEnv() {
        String provider = getenv("LLM_PROVIDER", "openai").toLowerCase();

        return switch (provider) {
            // Add implementations as you create them
            // case "ollama" -> new OllamaLlmClient(...);
            // case "anthropic" -> new AnthropicLlmClient(...);
            // case "gemini" -> new GeminiLlmClient(...);
            case "openai" -> new OpenAiLlmClient();
            default -> throw new IllegalArgumentException(
                    "Unsupported LLM_PROVIDER='" + provider + "'. Supported: openai"
            );
        };
    }

    private static String getenv(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}

