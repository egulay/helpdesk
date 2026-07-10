package com.helpdesk.mcp.client;

import com.openai.client.OpenAIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(
        prefix = "helpdesk.ai",
        name = "provider",
        havingValue = "lm-studio",
        matchIfMissing = true
)
public class LmStudioAiService extends AbstractAiService {

    public LmStudioAiService(
            @Qualifier("lmStudioClient") OpenAIClient client,
            @Value("${helpdesk.ai.lm-studio.model:qwen3-vl-8b-instruct}") String model,
            @Value("${helpdesk.ai.lm-studio.base-url:http://localhost:1234/v1}") String baseUrl
    ) {
        super(client, model);

        log.info("AI provider initialized");
        log.info("AI provider : LM Studio");
        log.info("AI model    : {}", model);
        log.info("AI endpoint : {}", baseUrl);
    }
}