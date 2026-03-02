package com.renatoback.module1;

import java.util.ArrayList;
import java.util.List;

import com.renatoback.module1.Message.Roles;

public class NoMemoryExample {

    public static void main(String[] args) {
        LLM llm = new LLM();

        // First request
        List<Message> messages = new ArrayList<>();
        messages.add(new Message(Roles.SYSTEM,
                "You are an expert software engineer that prefers functional programming."));
        messages.add(new Message(Roles.USER,
                "Write a Java function to swap the keys and values in a dictionary."));

        String response = llm.generateResponse(messages);
        System.out.println(response);

        System.out.println("\n\n--------------------------------\n\n");

        // We are going to make this verbose so it is clear what
        // is going on. In a real application, you would likely
        // just append to the messages list.
        List<Message> secondMessages = new ArrayList<>();
        
        // Now, when we ask the assistant to update the function, 
        // it will not have any memory of the previous interaction.
        secondMessages.add(new Message(Roles.USER,
                "Update the function to include documentation."));

        String secondResponse = llm.generateResponse(secondMessages);
        System.out.println(secondResponse);
    }
}