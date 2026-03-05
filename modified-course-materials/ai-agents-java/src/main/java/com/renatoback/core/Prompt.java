package com.renatoback.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to represent a prompt for the LLM, including messages and optional
 * tools
 */
public record Prompt(List<Message> messages, List<Tool> tools, Map<String, Object> metadata) {
    public Prompt(List<Message> messages) {
        this(messages, new ArrayList<>(), new HashMap<>());
    }

    public Prompt(List<Message> messages, List<Tool> tools) {
        this(messages, tools, new HashMap<>());
    }
}
