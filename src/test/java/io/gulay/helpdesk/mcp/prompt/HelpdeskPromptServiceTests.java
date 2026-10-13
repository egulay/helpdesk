package io.gulay.helpdesk.mcp.prompt;

import io.gulay.helpdesk.mcp.tools.assistant.HelpdeskTicketContext;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HelpdeskPromptServiceTests {

    private final HelpdeskPromptService promptService = new HelpdeskPromptService();

    private static HelpdeskTicketContext context() {
        return new HelpdeskTicketContext(
                new HelpdeskTicketContext.TicketRequest(
                        7, 1, "VPN disconnects every ten minutes", false, "2026-07-09T10:15:30Z", null
                ),
                new HelpdeskTicketContext.TicketRequester(
                        1, "Ada Lovelace", "ada@example.com", true, "2026-07-09T10:00:00Z"
                ),
                List.of(new HelpdeskTicketContext.TicketResponse(
                        3, 7, 1, "Asked for the client logs", "2026-07-09T11:00:00Z"
                ))
        );
    }

    @Test
    public void system_prompt_states_the_grounding_rules_test() {
        val prompt = promptService.systemPrompt();

        assertTrue(prompt.contains("You are a helpdesk assistant."));
        assertTrue(prompt.contains("Use only the provided helpdesk data."));
        assertTrue(prompt.contains("Do not invent requester information."));
    }

    @Test
    public void summarize_prompt_embeds_ticket_context_test() {
        val prompt = promptService.summarizePrompt(context());

        assertTrue(prompt.contains("Summarize this helpdesk issue."));
        assertTrue(prompt.contains("VPN disconnects every ten minutes"));
        assertTrue(prompt.contains("Asked for the client logs"));
        assertTrue(prompt.contains("recommended next action"));
    }

    @Test
    public void suggest_response_prompt_asks_for_a_grounded_draft_test() {
        val prompt = promptService.suggestResponsePrompt(context());

        assertTrue(prompt.contains("Draft a professional support response."));
        assertTrue(prompt.contains("avoid overpromising"));
        assertTrue(prompt.contains("ada@example.com"));
    }

    @Test
    public void classify_priority_prompt_pins_the_output_format_test() {
        val prompt = promptService.classifyPriorityPrompt(context());

        assertTrue(prompt.contains("Priority: LOW | MEDIUM | HIGH | CRITICAL"));
        assertTrue(prompt.contains("Reason: <short reason>"));
    }

    @Test
    public void estimate_category_prompt_lists_example_categories_test() {
        val prompt = promptService.estimateCategoryPrompt(context());

        assertTrue(prompt.contains("Category: <category>"));
        assertTrue(prompt.contains("- Network"));
        assertTrue(prompt.contains("- Hardware"));
    }

    @Test
    public void timeline_prompt_requires_chronological_output_test() {
        val prompt = promptService.timelinePrompt(context());

        assertTrue(prompt.contains("Generate a chronological timeline"));
        assertTrue(prompt.contains("each response in order"));
        assertTrue(prompt.contains("If timestamps are missing, say they are missing."));
    }

    @Test
    public void escalation_summary_prompt_requires_current_blocker_test() {
        val prompt = promptService.escalationSummaryPrompt(context());

        assertTrue(prompt.contains("Create an escalation summary"));
        assertTrue(prompt.contains("current blocker"));
        assertTrue(prompt.contains("Do not invent technical facts."));
    }

    @Test
    public void knowledge_base_prompt_requires_draft_marking_test() {
        val prompt = promptService.knowledgeBaseArticlePrompt(context());

        assertTrue(prompt.contains("Generate a draft knowledge base article"));
        assertTrue(prompt.contains("when to escalate"));
        assertTrue(prompt.contains("clearly mark the article as a draft"));
    }

    @Test
    public void every_prompt_carries_the_helpdesk_data_section_test() {
        val context = context();

        for (val prompt : List.of(
                promptService.summarizePrompt(context),
                promptService.suggestResponsePrompt(context),
                promptService.classifyPriorityPrompt(context),
                promptService.estimateCategoryPrompt(context),
                promptService.timelinePrompt(context),
                promptService.escalationSummaryPrompt(context),
                promptService.knowledgeBaseArticlePrompt(context)
        )) {
            assertTrue(prompt.contains("Helpdesk data:"));
            assertTrue(prompt.contains("Ada Lovelace"));
        }
    }
}
