package com.renatoback.core;

/**
 * Provider-agnostic interface for generating responses from an LLM.
 *
 * This file is part of the locally modified course material workspace.
 */
public interface LlmClient<T> {
    String generateResponse(T input);
}

