package com.helpdesk.mcp.client;

import com.openai.client.OpenAIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnExpression("${helpdesk.ai.enabled:true} and '${helpdesk.ai.provider:lm-studio}' == 'openai'")
public class OpenAiService extends AbstractAiService {

    public OpenAiService(
            @Qualifier("openAiClient") OpenAIClient client,
            @Value("${helpdesk.ai.openai.model:gpt-5.2}") String model,
            @Value("${helpdesk.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl
    ) {
        super(client, model);

        log.info("AI provider initialized");
        log.info("AI provider : OpenAI");
        log.info("AI model    : {}", model);
        log.info("AI endpoint : {}", baseUrl);
    }
}
