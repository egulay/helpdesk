package io.gulay.helpdesk.mcp.client;

import com.openai.client.OpenAIClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnExpression("${helpdesk.ai.enabled:true} and '${helpdesk.ai.provider:lm-studio}' == 'lm-studio'")
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
