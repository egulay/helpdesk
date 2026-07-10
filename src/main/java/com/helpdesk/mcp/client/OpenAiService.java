package com.helpdesk.mcp.client;

import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.ResponseCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiService implements AiService {

    private final OpenAIClient client;

    @Value("${helpdesk.ai.openai.model:gpt-5.2}")
    private String model;

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        val params = ResponseCreateParams.builder()
                .model(ChatModel.of(model))
                .instructions(systemPrompt)
                .input(userPrompt)
                .build();

        val response = client.responses().create(params);

        val result = new StringBuilder();

        response.output().stream()
                .flatMap(item -> item.message().stream())
                .flatMap(message -> message.content().stream())
                .flatMap(content -> content.outputText().stream())
                .forEach(text -> result.append(text.text()));

        return result.toString();
    }
}
