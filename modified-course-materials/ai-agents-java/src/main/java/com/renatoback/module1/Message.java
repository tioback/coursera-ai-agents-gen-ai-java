package com.renatoback.module1;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 * Modified: package changed to {@code com.renatoback.module1}.
 */
public class Message {

    public enum Roles { ASSISTANT, USER, SYSTEM }

    private Roles role;
    private String content;

    public Message(Roles role, String content) {
        this.role = role;
        this.content = content;
    }

    public static Message of(Roles role, String content) {
        return new Message(role, content);
    }

    public Roles getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    public void setRole(Roles role) {
        this.role = role;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

