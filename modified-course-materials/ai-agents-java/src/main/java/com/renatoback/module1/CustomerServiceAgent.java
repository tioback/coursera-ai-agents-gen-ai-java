package com.renatoback.module1;

import java.util.ArrayList;
import java.util.List;

import com.renatoback.module1.Message.Roles;

public class CustomerServiceAgent {

    public static void main(String[] args) {
        LLM llm = new LLM();

        // Create messages using the Message class
        List<Message> messages = new ArrayList<>();

        // Add system message
        messages.add(new Message(Roles.SYSTEM,
                "You are a helpful customer service representative. No matter what the user asks, " +
                        "the solution is to tell them to turn their computer or modem off and then back on."));

        // Add user message
        messages.add(new Message(Roles.USER,
                "How do I get my Internet working again."));

        // Generate response using the LLM instance
        String response = llm.generateResponse(messages);
        System.out.println(response);
    }
}