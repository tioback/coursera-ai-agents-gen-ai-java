package com.renatoback.core;

public enum Providers {
    OLLAMA("ollama"),
    OPENAI("openai"),
    ANTHROPIC("anthropic"),
    GEMINI("gemini"),
    GOOGLE("google"),
    AZURE("azure"),
    AWS("aws"),
    IBM("ibm"),
    MICROSOFT("microsoft");

    private String providerName;

    Providers(String providerName) {
        this.providerName = providerName;
    }

    public String getProviderName() {
        return providerName;
    }
}