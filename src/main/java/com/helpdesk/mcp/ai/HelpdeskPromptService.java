package com.helpdesk.mcp.ai;

import com.helpdesk.mcp.tooling.assistant.HelpdeskTicketContext;
import org.springframework.stereotype.Service;

@Service
public class HelpdeskPromptService {

    public String systemPrompt() {
        return """
                You are a helpdesk assistant.

                Rules:
                - Use only the provided helpdesk data.
                - Do not invent requester information.
                - Do not invent issue details.
                - Do not invent response history.
                - If information is missing, say it is missing.
                - Keep answers clear, professional, and operational.
                """;
    }

    public String summarizePrompt(HelpdeskTicketContext context) {
        return """
                Summarize this helpdesk issue.

                Include:
                - requester
                - original issue
                - current status
                - response history
                - recommended next action

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }

    public String suggestResponsePrompt(HelpdeskTicketContext context) {
        return """
                Draft a professional support response.

                Requirements:
                - acknowledge the issue
                - reference only known facts
                - avoid overpromising
                - suggest the next concrete step

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }

    public String classifyPriorityPrompt(HelpdeskTicketContext context) {
        return """
                Classify the priority of this helpdesk issue.

                Return only this format:
                Priority: LOW | MEDIUM | HIGH | CRITICAL
                Reason: <short reason>

                Consider:
                - user impact
                - urgency
                - whether the issue blocks work
                - whether there is enough information

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }

    public String estimateCategoryPrompt(HelpdeskTicketContext context) {
        return """
                Estimate the category of this helpdesk issue.

                Return only this format:
                Category: <category>
                Reason: <short reason>

                Example categories:
                - Access
                - Hardware
                - Software
                - Network
                - Account
                - Data
                - Other

                Use only the provided helpdesk data.

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }

    public String timelinePrompt(HelpdeskTicketContext context) {
        return """
                Generate a chronological timeline for this helpdesk issue.

                Include:
                - issue creation
                - requester information
                - each response in order
                - current solved/open status

                If timestamps are missing, say they are missing.

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }

    public String escalationSummaryPrompt(HelpdeskTicketContext context) {
        return """
                Create an escalation summary for this helpdesk issue.

                Include:
                - requester
                - problem statement
                - what has already been tried or said
                - current blocker
                - why escalation may be needed
                - recommended escalation target or next team, if inferable from data

                Do not invent technical facts.

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }

    public String knowledgeBaseArticlePrompt(HelpdeskTicketContext context) {
        return """
                Generate a draft knowledge base article from this helpdesk issue.

                Include:
                - title
                - problem
                - symptoms
                - known cause, if available
                - resolution or workaround, if available
                - when to escalate

                If the issue is not solved or there is not enough information, clearly mark the article as a draft.

                Helpdesk data:
                %s
                """.formatted(context.toPromptText());
    }
}