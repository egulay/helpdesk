package io.gulay.helpdesk.mcp.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@ConditionalOnProperty(prefix = "helpdesk.ai", name = "enabled", havingValue = "false")
public class DisabledAiService implements AiService {

    private static final String DISABLED_MESSAGE =
            "AI assistant tools are disabled by helpdesk.ai.enabled=false";

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        log.warn("Rejected AI assistant request: {}", DISABLED_MESSAGE);
        throw new IllegalStateException(DISABLED_MESSAGE);
    }
}
