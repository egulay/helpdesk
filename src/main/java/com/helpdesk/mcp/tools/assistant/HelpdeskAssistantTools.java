package com.helpdesk.mcp.tools.assistant;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HelpdeskAssistantTools {

    private final HelpdeskAssistantFacade facade;

    @Tool(description = "Summarize an issue request with requester and response history")
    public String summarizeIssueRequest(Integer requestId) {
        return facade.summarizeIssueRequest(requestId);
    }

    @Tool(description = "Suggest a professional support response for an issue request")
    public String suggestIssueResponse(Integer requestId) {
        return facade.suggestIssueResponse(requestId);
    }

    @Tool(description = "Classify the priority of an issue request as LOW, MEDIUM, HIGH, or CRITICAL")
    public String classifyIssuePriority(Integer requestId) {
        return facade.classifyIssuePriority(requestId);
    }

    @Tool(description = "Estimate the category of an issue request, such as Access, Hardware, Software, Network, Account, Data, or Other")
    public String estimateIssueCategory(Integer requestId) {
        return facade.estimateIssueCategory(requestId);
    }

    @Tool(description = "Generate a chronological timeline for an issue request")
    public String generateIssueTimeline(Integer requestId) {
        return facade.generateIssueTimeline(requestId);
    }

    @Tool(description = "Create an escalation summary for an issue request")
    public String createEscalationSummary(Integer requestId) {
        return facade.createEscalationSummary(requestId);
    }

    @Tool(description = "Generate a draft knowledge base article from an issue request")
    public String generateKnowledgeBaseArticle(Integer requestId) {
        return facade.generateKnowledgeBaseArticle(requestId);
    }
}
