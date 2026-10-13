package io.gulay.helpdesk.mcp.tools.assistant;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.model.IssueResponseModel;
import io.gulay.helpdesk.mcp.client.AiService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.gulay.helpdesk.mcp.prompt.HelpdeskPromptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HelpdeskAssistantIntegrationTests extends TestBase {

    /**
     * Records the prompts the facade builds so the assistant chain can be asserted
     * without calling a real AI provider.
     */
    public static class RecordingAiService implements AiService {
        private final List<String> systemPrompts = new ArrayList<>();
        private final List<String> userPrompts = new ArrayList<>();

        @Override
        public String chat(String systemPrompt, String userPrompt) {
            systemPrompts.add(systemPrompt);
            userPrompts.add(userPrompt);
            return "stubbed assistant answer";
        }

        String lastSystemPrompt() {
            return systemPrompts.getLast();
        }

        String lastUserPrompt() {
            return userPrompts.getLast();
        }

        int callCount() {
            return userPrompts.size();
        }
    }

    @Autowired
    private HelpdeskContextBuilder contextBuilder;

    @Autowired
    private HelpdeskPromptService promptService;

    @Autowired
    private HelpdeskAssistantTools contextAssistantTools;

    private RecordingAiService recordingAiService;

    private HelpdeskAssistantTools assistantTools;

    private IssueRequesterModel requester;
    private IssueRequestModel request;

    @BeforeEach
    public void setup() {
        issueResponseService.hardDeleteAll();
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();

        recordingAiService = new RecordingAiService();
        assistantTools = new HelpdeskAssistantTools(new HelpdeskAssistantFacade(
                contextBuilder,
                promptService,
                recordingAiService
        ));

        requester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("Ada Lovelace")
                .isActive(true)
                .email(UUID.randomUUID() + "@email.com")
                .build());

        request = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(requester)
                .isSolved(false)
                .body("VPN disconnects every ten minutes")
                .build());
    }

    private void insertResponse(String body) {
        issueResponseService.save(IssueResponseModel
                .builder()
                .requester(requester)
                .request(request)
                .body(body)
                .build());
    }

    @Test
    public void assistant_chain_returns_the_provider_answer_test() {
        assertEquals("stubbed assistant answer", assistantTools.summarizeIssueRequest(request.getId()));
        assertEquals(1, recordingAiService.callCount());
    }

    @Test
    public void application_wiring_rejects_assistant_tools_while_ai_is_disabled_test() {
        // The test profile sets helpdesk.ai.enabled=false, so the context wires DisabledAiService.
        val ex = assertThrows(
                IllegalStateException.class,
                () -> contextAssistantTools.summarizeIssueRequest(request.getId())
        );

        assertEquals("AI assistant tools are disabled by helpdesk.ai.enabled=false", ex.getMessage());
    }

    @Test
    public void assistant_prompts_carry_the_grounding_system_prompt_test() {
        assistantTools.summarizeIssueRequest(request.getId());

        assertTrue(recordingAiService.lastSystemPrompt().contains("You are a helpdesk assistant."));
        assertTrue(recordingAiService.lastSystemPrompt().contains("Use only the provided helpdesk data."));
    }

    @Test
    public void ticket_context_includes_requester_request_and_responses_test() {
        insertResponse("Asked for the client logs");
        insertResponse("Rolled out a new VPN profile");

        assistantTools.summarizeIssueRequest(request.getId());

        val prompt = recordingAiService.lastUserPrompt();
        assertTrue(prompt.contains("Ada Lovelace"));
        assertTrue(prompt.contains(requester.getEmail()));
        assertTrue(prompt.contains("VPN disconnects every ten minutes"));
        assertTrue(prompt.contains("Asked for the client logs"));
        assertTrue(prompt.contains("Rolled out a new VPN profile"));
        assertTrue(prompt.contains("requesterId: " + requester.getId()));
    }

    @Test
    public void ticket_context_reports_missing_response_history_test() {
        assistantTools.summarizeIssueRequest(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("No responses found."));
    }

    @Test
    public void ticket_context_reflects_solved_state_test() {
        issueRequestService.solveIssue(request.getId());

        assistantTools.generateIssueTimeline(request.getId());

        val prompt = recordingAiService.lastUserPrompt();
        assertTrue(prompt.contains("isSolved: true"));
        assertFalse(prompt.contains("solved: null"));
    }

    @Test
    public void summarize_tool_uses_the_summarize_prompt_test() {
        assistantTools.summarizeIssueRequest(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("Summarize this helpdesk issue."));
    }

    @Test
    public void suggest_response_tool_uses_the_response_prompt_test() {
        assistantTools.suggestIssueResponse(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("Draft a professional support response."));
    }

    @Test
    public void classify_priority_tool_uses_the_priority_prompt_test() {
        assistantTools.classifyIssuePriority(request.getId());

        assertTrue(recordingAiService.lastUserPrompt()
                .contains("Priority: LOW | MEDIUM | HIGH | CRITICAL"));
    }

    @Test
    public void estimate_category_tool_uses_the_category_prompt_test() {
        assistantTools.estimateIssueCategory(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("Category: <category>"));
    }

    @Test
    public void timeline_tool_uses_the_timeline_prompt_test() {
        assistantTools.generateIssueTimeline(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("Generate a chronological timeline"));
    }

    @Test
    public void escalation_tool_uses_the_escalation_prompt_test() {
        assistantTools.createEscalationSummary(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("Create an escalation summary"));
    }

    @Test
    public void knowledge_base_tool_uses_the_article_prompt_test() {
        assistantTools.generateKnowledgeBaseArticle(request.getId());

        assertTrue(recordingAiService.lastUserPrompt().contains("Generate a draft knowledge base article"));
    }

    @Test
    public void every_assistant_tool_builds_its_own_prompt_test() {
        val requestId = request.getId();

        assistantTools.summarizeIssueRequest(requestId);
        assistantTools.suggestIssueResponse(requestId);
        assistantTools.classifyIssuePriority(requestId);
        assistantTools.estimateIssueCategory(requestId);
        assistantTools.generateIssueTimeline(requestId);
        assistantTools.createEscalationSummary(requestId);
        assistantTools.generateKnowledgeBaseArticle(requestId);

        assertEquals(7, recordingAiService.callCount());
    }

    @Test
    public void assistant_tools_reject_unknown_request_ids_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> assistantTools.summarizeIssueRequest(999999)
        );

        assertEquals(0, recordingAiService.callCount());
    }
}
