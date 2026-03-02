package com.renatoback.module1;

import java.util.Arrays;

/**
 * Simple factory for selecting an {@link LlmClient} at runtime.
 *
 * Provider and Model selection is controlled via environment variables:
 * - LLM_MODEL: OLLAMA_LLAMA2_LATEST (default), OLLAMA_QWEN_EXPERT, OLLAMA_QWEN2_5_CODER_14B, OLLAMA_QWEN2_5_CODER_LATEST
 *
 * Provider-specific configuration is intentionally minimal here and can be expanded
 * as you add more implementations.
 */
public final class LlmClients {

    public enum Providers {
        OLLAMA("ollama"),
        OPENAI("openai"),
        ANTHROPIC("anthropic"),
        GEMINI("gemini"),
        GOOGLE("google"),
        AZURE("azure"),
        AWS("aws"),
        IBM("ibm"),
        MICROSOFT("microsoft");

        private String providerName;

        Providers(String providerName) {
            this.providerName = providerName;
        }

        public String getProviderName() {
            return providerName;
        }
    }

    public enum Models {
        OLLAMA_QWEN_EXPERT(Providers.OLLAMA, "qwen-expert:latest"),
        OLLAMA_QWEN2_5_CODER_14B(Providers.OLLAMA, "qwen2.5-coder:14b"),
        OLLAMA_QWEN2_5_CODER_LATEST(Providers.OLLAMA, "qwen2.5-coder:latest"),
        OLLAMA_LLAMA2_LATEST(Providers.OLLAMA, "llama2:latest");

        private Providers client;
        private String modelName;

        Models(Providers client, String modelName) {
            this.client = client;
            this.modelName = modelName;
        }

        public Providers getClient() {
            return client;
        }

        public String getModelName() {
            return modelName;
        }
    }
    

    private LlmClients() {
    }

    public static LlmClient fromEnv() {
        Models model = Models.valueOf(getenv("LLM_MODEL", Models.OLLAMA_LLAMA2_LATEST.name()).toUpperCase());

        return switch (model.getClient()) {
            case Providers.OLLAMA -> new OllamaLlmClient(
                    getenv("OLLAMA_BASE_URL", "http://localhost:11434"),
                    model.getModelName()
            );
            case Providers.OPENAI -> new OpenAiLlmClient();
            default -> throw new IllegalArgumentException(
                    "Unsupported LLM_MODEL='" + model.name() + "'. Supported: " + Arrays.toString(Models.values())
            );
        };
    }

    private static String getenv(String name, String defaultValue) {
        return DotEnvConfig.get(name, defaultValue);
    }
}

