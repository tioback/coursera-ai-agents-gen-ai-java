package com.renatoback.module1;

import java.util.List;

/**
 * Provider-agnostic interface for generating responses from an LLM.
 *
 * This file is part of the locally modified course material workspace.
 */
public interface LlmClient {
    String generateResponse(List<Message> messages);
}

