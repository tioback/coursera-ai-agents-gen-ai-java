package com.renatoback.module1;

import java.util.List;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 *
 * Modified: this class is now provider-pluggable via {@link LlmClient}.
 * The default constructor selects a provider using {@link LlmClients#fromEnv()}.
 */
public class LLM {

    private final LlmClient client;
    private boolean isDebugging;

    public LLM() {
        this(LlmClients.fromEnv());
        isDebugging = Boolean.parseBoolean(System.getenv("DEBUG_MODE"));
    }

    public LLM(LlmClient client) {
        this.client = client;
    }

    /**
     * Generates an LLM response based on the provided messages.
     *
     * @param messages List of Message objects containing role and content.
     * @return The generated response as a String.
     */
    public String generateResponse(List<Message> messages) {
        if (isDebugging) {
            System.err.println("PROMPT: " + messages);
        }
        String response = client.generateResponse(messages);
        if (isDebugging) {
            System.err.println("RESPONSE: " + response);
        }
        return response;
    }
}

