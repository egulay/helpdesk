package com.helpdesk.mcp.client;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputText;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public abstract class AbstractAiService implements AiService {

    private final OpenAIClient client;
    private final String model;

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        validatePrompt(systemPrompt, "systemPrompt");
        validatePrompt(userPrompt, "userPrompt");

        val params = ResponseCreateParams.builder()
                .model(ChatModel.of(model))
                .instructions(systemPrompt)
                .input(userPrompt)
                .build();

        val response = client.responses().create(params);

        val result = response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .map(ResponseOutputText::text)
                .filter(text -> !text.isBlank())
                .reduce("", String::concat);

        if (result.isBlank()) {
            throw new IllegalStateException(
                    "The AI provider returned no textual response."
            );
        }

        return result;
    }

    private void validatePrompt(String prompt, String parameterName) {
        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException(
                    parameterName + " must not be null or blank."
            );
        }
    }
}