package com.renatoback.module1;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 * Modified: package changed to {@code com.renatoback.module1}.
 */
public class Message {

    private String role;
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public static Message of(String role, String content) {
        return new Message(role, content);
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

