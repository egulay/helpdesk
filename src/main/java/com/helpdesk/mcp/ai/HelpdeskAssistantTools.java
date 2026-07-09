/// AI orchestrator
/// Fully LLM related tasks could be here to improve the functionalit, not the CRUD - such as:
/// summarizeIssue()
/// suggestResponse()
/// classifyIssuePriority()
/// estimateIssueCategory()
/// findSimilarIssues()
/// generateRequesterSummary()
/// generateIssueTimeline()
/// detectDuplicateIssue()
/// explainIssue()
/// createEscalationSummary()
/// generateKnowledgeBaseArticle()
/// translateResponse()
/// rewriteResponse()
/// generateManagerSummary()
package com.helpdesk.mcp.ai;

import com.helpdesk.mcp.assistant.HelpdeskAssistantFacade;
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
}