package com.renatoback.module1;

import java.util.ArrayList;
import java.util.List;

import com.renatoback.core.LLM;
import com.renatoback.core.Message;
import com.renatoback.core.Message.Roles;

public class CustomerServiceAgent {

    public static void main(String[] args) {
        LLM<List<Message>> llm = LLM.fromEnv();

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