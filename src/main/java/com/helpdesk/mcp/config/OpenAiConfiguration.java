package com.helpdesk.mcp.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfiguration {

    private static final String LM_STUDIO_PLACEHOLDER_KEY = "lm-studio-local";

    @Bean("openAiClient")
    @ConditionalOnProperty(
            prefix = "helpdesk.ai",
            name = "provider",
            havingValue = "openai"
    )
    public OpenAIClient openAiClient(
            @Value("${helpdesk.ai.openai.api-key:}") String apiKey,
            @Value("${helpdesk.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "Missing valid OpenAI API key from Vault property: helpdesk.ai.openai.api-key"
            );
        }

        return OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .maxRetries(3)
                .build();
    }

    @Bean("lmStudioClient")
    @ConditionalOnProperty(
            prefix = "helpdesk.ai",
            name = "provider",
            havingValue = "lm-studio",
            matchIfMissing = true
    )
    public OpenAIClient lmStudioClient(
            @Value("${helpdesk.ai.lm-studio.base-url:http://localhost:1234/v1}") String baseUrl
    ) {
        return OpenAIOkHttpClient.builder()
                .apiKey(LM_STUDIO_PLACEHOLDER_KEY)
                .baseUrl(baseUrl)
                .maxRetries(2)
                .build();
    }
}