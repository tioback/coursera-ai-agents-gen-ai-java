package com.renatoback.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

// Simplified Tool class for representing tool definitions
public record Tool(String toolName, String description, Map<String, Object> parameters) {

    /**
     * Converts the Tool object to a JSON string
     *
     * @return JSON string representation of the Tool
     */
    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert Tool to JSON", e);
        }
    }

    /**
     * Creates a Tool from a JSON string
     *
     * @param json The JSON string representing the tool
     * @return A new Tool instance
     * @throws Exception If the JSON cannot be parsed
     */
    public static Tool fromJson(String json) {
        try{
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, Tool.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to Tool", e);
        }
    }
}