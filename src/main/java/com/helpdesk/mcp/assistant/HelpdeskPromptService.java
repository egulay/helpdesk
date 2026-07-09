package com.helpdesk.mcp.assistant;

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
}