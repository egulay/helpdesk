package io.gulay.helpdesk.mcp.tools.data;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.model.IssueResponseModel;
import io.gulay.helpdesk.mcp.dto.IssueRequestToolRequest;
import io.gulay.helpdesk.mcp.dto.IssueRequesterToolRequest;
import io.gulay.helpdesk.mcp.dto.IssueResponseToolRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(properties = "helpdesk.mcp.allow-mutations=true")
public class HelpdeskMutationToolsIntegrationTests extends TestBase {

    @Autowired
    private HelpdeskMutationTools mutationTools;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    private IssueRequesterModel requester;
    private IssueRequestModel request;

    @BeforeEach
    public void setup() {
        issueResponseService.hardDeleteAll();
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();

        requester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("mutation_requester")
                .isActive(true)
                .email(UUID.randomUUID() + "@email.com")
                .build());

        request = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(requester)
                .isSolved(false)
                .body("Mutation target issue")
                .build());
    }

    @Test
    public void mutation_tools_are_registered_when_explicitly_allowed_test() {
        val toolNames = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertTrue(toolNames.contains("saveIssueRequester"));
        assertTrue(toolNames.contains("toggleIssueRequesterActivation"));
        assertTrue(toolNames.contains("hardDeleteIssueRequester"));
        assertTrue(toolNames.contains("solveIssueRequest"));
        assertTrue(toolNames.contains("saveIssueRequest"));
        assertTrue(toolNames.contains("hardDeleteIssueRequest"));
        assertTrue(toolNames.contains("saveIssueResponse"));
        assertTrue(toolNames.contains("hardDeleteIssueResponse"));

        // Read-only tools stay available alongside the mutation tools.
        assertTrue(toolNames.contains("findIssueRequesterById"));
    }

    @Test
    public void toggle_issue_requester_activation_test() {
        val toggled = mutationTools.toggleIssueRequesterActivation(requester.getId());

        assertFalse(toggled.isActive());
        assertFalse(issueRequesterService.findById(requester.getId()).getIsActive());
    }

    @Test
    public void save_issue_requester_creates_record_test() {
        val created = mutationTools.saveIssueRequester(new IssueRequesterToolRequest(
                null, "Created Through Mcp", "mcp-created@email.com", true));

        assertNotNull(created.id());
        assertEquals("Created Through Mcp", issueRequesterService.findById(created.id()).getFullName());
    }

    @Test
    public void save_issue_requester_updates_record_test() {
        val updated = mutationTools.saveIssueRequester(new IssueRequesterToolRequest(
                requester.getId(), "Renamed Through Mcp", requester.getEmail(), true));

        assertEquals(requester.getId(), updated.id());
        assertEquals("Renamed Through Mcp", updated.fullName());
    }

    @Test
    public void hard_delete_issue_requester_cascades_to_requests_test() {
        val deleted = mutationTools.hardDeleteIssueRequester(requester.getId());

        assertEquals(requester.getId(), deleted.id());
        assertThrows(
                ResponseStatusException.class,
                () -> issueRequesterService.findById(requester.getId())
        );
        assertFalse(issueRequestService.isExists(request.getId()));
    }

    @Test
    public void solve_issue_request_test() {
        val solved = mutationTools.solveIssueRequest(request.getId());

        assertTrue(solved.isSolved());
        assertNotNull(solved.solved());
    }

    @Test
    public void save_issue_request_creates_record_test() {
        val created = mutationTools.saveIssueRequest(new IssueRequestToolRequest(
                null, requester.getId(), "Created through mcp", false, null));

        assertNotNull(created.id());
        assertTrue(issueRequestService.isExists(created.id()));
    }

    @Test
    public void hard_delete_issue_request_test() {
        val deleted = mutationTools.hardDeleteIssueRequest(request.getId());

        assertEquals(request.getId(), deleted.id());
        assertFalse(issueRequestService.isExists(request.getId()));
    }

    @Test
    public void save_issue_response_creates_record_test() {
        val created = mutationTools.saveIssueResponse(new IssueResponseToolRequest(
                null, request.getId(), requester.getId(), "Created through mcp"));

        assertNotNull(created.id());
        assertEquals("Created through mcp", issueResponseService.findById(created.id()).getBody());
    }

    @Test
    public void hard_delete_issue_response_test() {
        val saved = issueResponseService.save(IssueResponseModel
                .builder()
                .requester(requester)
                .request(request)
                .body("Delete me through mcp")
                .build());

        val deleted = mutationTools.hardDeleteIssueResponse(saved.getId());

        assertEquals(saved.getId(), deleted.id());
        assertThrows(
                ResponseStatusException.class,
                () -> issueResponseService.findById(saved.getId())
        );
    }

    @Test
    public void mutation_tools_propagate_not_found_errors_test() {
        assertThrows(ResponseStatusException.class, () -> mutationTools.solveIssueRequest(999999));
        assertThrows(ResponseStatusException.class, () -> mutationTools.hardDeleteIssueRequest(999999));
        assertThrows(ResponseStatusException.class, () -> mutationTools.hardDeleteIssueRequester(999999));
        assertThrows(ResponseStatusException.class, () -> mutationTools.hardDeleteIssueResponse(999999));
    }
}
