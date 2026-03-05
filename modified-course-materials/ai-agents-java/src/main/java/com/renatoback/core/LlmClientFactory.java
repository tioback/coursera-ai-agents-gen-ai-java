package com.renatoback.core;

import java.util.Arrays;
import java.util.List;

import com.renatoback.module1.OllamaLlmClient;
import com.renatoback.module1.OpenAiLlmClient;
import com.renatoback.module2.OllamaFunctionCallingLlmClient;
import com.renatoback.module2.OpenAiFunctionCallingLlmClient;

/**
 * Factory for providing the appropriate {@link LlmClient} implementation to the
 * {@link LLM} constructor.
 * <p>
 * Factory methods use environment variables (e.g. {@code LLM_MODEL}) to select
 * the provider and model. They differ only by the type of input they support:
 * {@link #forMessage()} returns a client for {@link Message} lists, while
 * {@link #forPrompt()} returns a client for {@link Prompt} (messages + tools).
 */
public final class LlmClientFactory {

    private LlmClientFactory() {
    }

    /**
     * Returns a client that operates on {@link Message} lists. Uses env to pick
     * provider and model (e.g. Ollama, OpenAI).
     */
    public static LlmClient<List<Message>> forMessages() {
        Models model = resolveModel();
        return switch (model.getClient()) {
            case Providers.OLLAMA -> new OllamaLlmClient(
                    getenv("OLLAMA_BASE_URL", "http://localhost:11434"),
                    model.getModelName()
            );
            case Providers.OPENAI -> new OpenAiLlmClient();
            default -> throw new IllegalArgumentException(
                    "Unsupported LLM_MODEL for Message: " + model.name()
                            + ". Supported: " + Arrays.toString(Models.values())
            );
        };
    }

    /**
     * Returns a client that operates on {@link Prompt} (messages plus optional
     * tools). Uses env to pick provider and model. Function-calling support
     * depends on provider.
     */
    public static LlmClient<Prompt> forPrompt() {
        Models model = resolveModel();
        return switch (model.getClient()) {
            case Providers.OPENAI -> new OpenAiFunctionCallingLlmClient();
            case Providers.OLLAMA -> new OllamaFunctionCallingLlmClient(
                    getenv("OLLAMA_BASE_URL", "http://localhost:11434"),
                    model.getModelName()
            );
            default -> throw new IllegalArgumentException(
                    "Unsupported LLM_MODEL for Prompt: " + model.name()
                            + ". Supported: " + Arrays.toString(Models.values())
            );
        };
    }

    private static Models resolveModel() {
        return Models.valueOf(
                getenv("LLM_MODEL", Models.OLLAMA_QWEN_EXPERT.name()).toUpperCase()
        );
    }

    private static String getenv(String name, String defaultValue) {
        return DotEnvConfig.get(name, defaultValue);
    }
}
