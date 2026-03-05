package com.renatoback.module2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.JsonValue;
import com.openai.models.ChatModel;
import com.openai.models.FunctionDefinition;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageToolCall;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionTool;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.renatoback.core.LlmClient;
import com.renatoback.core.Message;
import com.renatoback.core.Message.Roles;
import com.renatoback.core.Prompt;
import com.renatoback.core.Tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link LlmClient} implementation for {@link Prompt} that uses the OpenAI API
 * with optional function/tool calling.
 */
public class OpenAiFunctionCallingLlmClient implements LlmClient<Prompt> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ChatModel model;
    private final int maxTokens;

    public OpenAiFunctionCallingLlmClient() {
        this(ChatModel.GPT_4_1, 1024);
    }

    public OpenAiFunctionCallingLlmClient(ChatModel model, int maxTokens) {
        this.model = model;
        this.maxTokens = maxTokens;
    }

    @Override
    public String generateResponse(Prompt prompt) {
        if (prompt == null) {
            throw new IllegalArgumentException("prompt must not be null");
        }

        OpenAIClient client = OpenAIOkHttpClient.fromEnv();

        List<Message> messages = prompt.messages();
        List<Tool> tools = prompt.tools();

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
                .model(model)
                .maxCompletionTokens(maxTokens);

        for (Message message : messages) {
            Roles role = message.role();
            if (Roles.SYSTEM.equals(role)) {
                paramsBuilder.addMessage(
                        ChatCompletionSystemMessageParam.builder().content(message.content()).build());
            } else if (Roles.USER.equals(role)) {
                paramsBuilder.addMessage(
                        ChatCompletionUserMessageParam.builder().content(message.content()).build());
            } else {
                paramsBuilder.addMessage(
                        ChatCompletionAssistantMessageParam.builder().content(message.content()).build());
            }
        }

        if (tools != null && !tools.isEmpty()) {
            List<ChatCompletionTool> chatCompletionTools = tools.stream()
                    .map(this::toChatCompletionTool)
                    .toList();
            paramsBuilder.tools(chatCompletionTools);
        }

        try {
            ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());

            if (tools != null && !tools.isEmpty()
                    && completion.choices().get(0).message().toolCalls() != null
                    && !completion.choices().get(0).message().toolCalls().isEmpty()) {
                ChatCompletionMessageToolCall toolCall = completion.choices().get(0).message().toolCalls().get().get(0);
                Map<String, Object> toolResponse = new HashMap<>();
                toolResponse.put("tool", toolCall.function().name());
                toolResponse.put("args", OBJECT_MAPPER.readValue(toolCall.function().arguments(), Map.class));
                return OBJECT_MAPPER.writeValueAsString(toolResponse);
            }

            return completion.choices().get(0).message().content().orElse("");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate response", e);
        }
    }

    @SuppressWarnings("unchecked")
    private ChatCompletionTool toChatCompletionTool(Tool tool) {
        return ChatCompletionTool.builder()
                .type(JsonValue.from("function"))
                .function(FunctionDefinition.builder()
                        .name(tool.toolName())
                        .description(tool.description())
                        .parameters(JsonValue.from(tool.parameters()))
                        .build())
                .build();
    }
}
