package com.renatoback.module1;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;

import java.util.List;

/**
 * Derived from the Coursera "Building AI Agents in Java" course materials.
 * Modified: extracted OpenAI-specific logic behind {@link LlmClient}.
 */
public class OpenAiLlmClient implements LlmClient {

    private final ChatModel model;
    private final int maxTokens;

    public OpenAiLlmClient() {
        this(ChatModel.GPT_4_1, 1024);
    }

    public OpenAiLlmClient(ChatModel model, int maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @Override
    public String generateResponse(List<Message> messages) {
        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens);

        for (Message message : messages) {
            String role = message.getRole();
            if ("system".equals(role)) {
                ChatCompletionSystemMessageParam systemMsg = ChatCompletionSystemMessageParam.builder()
                        .content(message.getContent())
                        .build();
                paramsBuilder.addMessage(systemMsg);
            } else if ("user".equals(role)) {
                ChatCompletionUserMessageParam userMsg = ChatCompletionUserMessageParam.builder()
                        .content(message.getContent())
                        .build();
                paramsBuilder.addMessage(userMsg);
            } else {
                ChatCompletionAssistantMessageParam assistantMsg = ChatCompletionAssistantMessageParam.builder()
                        .content(message.getContent())
                        .build();
                paramsBuilder.addMessage(assistantMsg);
            }
        }

        ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());
        return completion.choices().get(0).message().content().orElse("");
    }
}

