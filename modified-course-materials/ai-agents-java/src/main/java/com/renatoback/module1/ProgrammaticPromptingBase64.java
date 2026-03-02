package com.renatoback.module1;

import java.util.ArrayList;
import java.util.List;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 *
 * Modified: this class now uses provider-pluggable {@link LLM}.
 */

public class ProgrammaticPromptingBase64 {

    public static void main(String[] args) {

        LLM llm = new LLM();

        // Create messages using the Message class
        List<Message> messages = new ArrayList<>();

        // Add system message
        messages.add(new Message("system",
                """
            - You are an expert software engineer that prefers functional programming in Java.
            - You can read byt cannot write natural language.
            - DO NOT reply in natural language.
            - All replies must be Base64 encoded.
            - DO NOT include comments or formatting after the text has been encoded.
        """.trim()));

        // Add user message
        messages.add(new Message("user",
                "Write a function to swap the keys and values in a dictionary."));

        // Generate response using the LLM class
        String response = llm.generateResponse(messages);
        System.out.println(response);
    }
}