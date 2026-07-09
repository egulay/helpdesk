package com.helpdesk.mcp.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
public class OpenAiConfiguration {

    @Bean
    public OpenAIClient openAIClient(
            @Value("${helpdesk.ai.openai.api-key}") String apiKey
    ) {
        if (Objects.isNull(apiKey) || apiKey.isBlank() || "change-me".equals(apiKey)) {
            throw new IllegalStateException("Missing valid OpenAI API key from Vault property: helpdesk.ai.openai.api-key");
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}