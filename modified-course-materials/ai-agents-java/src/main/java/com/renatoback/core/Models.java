package com.renatoback.core;

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