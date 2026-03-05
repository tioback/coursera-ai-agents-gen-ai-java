package com.renatoback.core;

import java.util.List;

public class LLM<T> {

    private final LlmClient<T> client;
    private final boolean isDebugging;
    
    public LLM(LlmClient<T> client) {
        this.client = client;
        this.isDebugging = Boolean.parseBoolean(System.getenv("DEBUG_MODE"));
    }

    /**
     * Convenience factory for the {@link Message}-based use case.
     * Uses {@link LlmClientFactory#forMessage()} and env for provider/model selection.
     */
    public static LLM<List<Message>> fromEnv() {
        return new LLM<>(LlmClientFactory.forMessages());
    }

    /**
     * Convenience factory for the {@link Prompt}-based use case
     * (messages + optional tools). Uses {@link LlmClientFactory#forPrompt()} and env
     * for provider/model selection.
     */
    public static LLM<Prompt> promptFromEnv() {
        return new LLM<>(LlmClientFactory.forPrompt());
    }

    /**
     * Generates an LLM response based on the provided messages.
     *
     * @param messages List of Message objects containing role and content.
     * @return The generated response as a String.
     */
    public String generateResponse(T input) {
        if (isDebugging) {
            System.err.println("PROMPT: " + input);
        }
        String response = client.generateResponse(input);
        if (isDebugging) {
            System.err.println("RESPONSE: " + response);
        }
        return response;
    }
}

