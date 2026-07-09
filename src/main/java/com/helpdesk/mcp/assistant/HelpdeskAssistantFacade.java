package com.helpdesk.mcp.assistant;

import com.helpdesk.mcp.ai.AiService;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class HelpdeskAssistantFacade {

    private final HelpdeskContextBuilder contextBuilder;
    private final HelpdeskPromptService promptService;
    private final AiService aiService;

    public HelpdeskAssistantFacade(
            HelpdeskContextBuilder contextBuilder,
            HelpdeskPromptService promptService,
            AiService aiService
    ) {
        this.contextBuilder = contextBuilder;
        this.promptService = promptService;
        this.aiService = aiService;
    }

    public String summarizeIssueRequest(Integer requestId) {
        val context = contextBuilder.buildTicketContext(requestId);

        return aiService.chat(
                promptService.systemPrompt(),
                promptService.summarizePrompt(context)
        );
    }

    public String suggestIssueResponse(Integer requestId) {
        val context = contextBuilder.buildTicketContext(requestId);

        return aiService.chat(
                promptService.systemPrompt(),
                promptService.suggestResponsePrompt(context)
        );
    }
}