package com.renatoback.module1;

import java.util.ArrayList;
import java.util.List;

import com.renatoback.module1.Message.Roles;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 *
 * Modified: this class now uses provider-pluggable {@link LLM}.
 */

public class ProgrammaticPrompting {

    public static void main(String[] args) {

        LLM llm = new LLM();

        // Create messages using the Message class
        List<Message> messages = new ArrayList<>();

        // Add system message
        messages.add(new Message(Roles.SYSTEM,
                "You are an expert software engineer that prefers functional programming."));

        // Add user message
        messages.add(new Message(Roles.USER,
                "Write a function to swap the keys and values in a dictionary."));

        // Generate response using the LLM class
        String response = llm.generateResponse(messages);
        System.out.println(response);
    }
}