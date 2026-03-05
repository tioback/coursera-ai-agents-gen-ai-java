package com.renatoback.core;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 * Modified: package changed to {@code com.renatoback.module1}.
 */
public record Message(Roles role, String content) {

    public enum Roles { ASSISTANT, USER, SYSTEM }


    public static Message of(Roles role, String content) {
        return new Message(role, content);
    }

}

