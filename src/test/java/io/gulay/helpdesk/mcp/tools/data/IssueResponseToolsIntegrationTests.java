package io.gulay.helpdesk.mcp.tools.data;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.model.IssueResponseModel;
import io.gulay.helpdesk.data.util.SortDirection;
import io.gulay.helpdesk.mcp.dto.IssueResponseToolRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IssueResponseToolsIntegrationTests extends TestBase {

    private static final String CREATED_AFTER = Instant.now().minus(1, ChronoUnit.DAYS).toString();
    private static final String CREATED_BEFORE = Instant.now().plus(1, ChronoUnit.DAYS).toString();

    @Autowired
    private IssueResponseTools responseTools;

    private IssueRequesterModel requester;
    private IssueRequestModel request;

    @BeforeEach
    public void setup() {
        issueResponseService.hardDeleteAll();
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();

        requester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("response_tools_requester")
                .isActive(true)
                .email(UUID.randomUUID() + "@email.com")
                .build());

        request = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(requester)
                .isSolved(false)
                .body("Issue needing responses")
                .build());
    }

    private IssueResponseModel insertResponse(String body) {
        return issueResponseService.save(IssueResponseModel
                .builder()
                .requester(requester)
                .request(request)
                .body(body)
                .build());
    }

    @Test
    public void find_issue_response_by_id_maps_all_fields_test() {
        val saved = insertResponse("We restarted the spooler");

        val response = responseTools.findIssueResponseById(saved.getId());

        assertEquals(saved.getId(), response.id());
        assertEquals(request.getId(), response.requestId());
        assertEquals(requester.getId(), response.requesterId());
        assertEquals("We restarted the spooler", response.responseBody());
        assertNotNull(response.created());
        assertDoesNotThrow(() -> Instant.parse(response.created()));
    }

    @Test
    public void find_issue_response_by_unknown_id_throws_test() {
        assertThrows(ResponseStatusException.class, () -> responseTools.findIssueResponseById(999999));
    }

    @Test
    public void find_all_issue_responses_returns_paged_response_test() {
        insertResponse("first");
        insertResponse("second");

        val response = responseTools.findAllIssueResponses(0, 10, "id", SortDirection.Ascending);

        assertEquals(2, response.totalElements());
        assertEquals(2, response.content().size());
        assertEquals(1, response.totalPages());
    }

    @Test
    public void find_all_issue_responses_on_empty_table_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> responseTools.findAllIssueResponses(0, 10, "id", SortDirection.Ascending)
        );
    }

    @Test
    public void find_issue_responses_by_created_date_range_test() {
        insertResponse("in range");

        val response = responseTools.findIssueResponsesByCreatedDateRange(
                CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Descending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void find_issue_responses_by_request_id_test() {
        insertResponse("first");
        insertResponse("second");

        val response = responseTools.findIssueResponsesByRequestId(
                request.getId(), 0, 10, "id", SortDirection.Ascending);

        assertEquals(2, response.totalElements());
        assertTrue(response.content().stream()
                .allMatch(item -> item.requestId().equals(request.getId())));
    }

    @Test
    public void find_issue_responses_by_unknown_request_id_throws_test() {
        insertResponse("first");

        assertThrows(
                ResponseStatusException.class,
                () -> responseTools.findIssueResponsesByRequestId(
                        999999, 0, 10, "id", SortDirection.Ascending)
        );
    }

    @Test
    public void find_issue_responses_by_request_id_and_created_date_range_test() {
        insertResponse("in range");

        val response = responseTools.findIssueResponsesByRequestIdAndCreatedDateRange(
                request.getId(), CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void find_issue_responses_by_requester_id_test() {
        insertResponse("first");

        val response = responseTools.findIssueResponsesByRequesterId(
                requester.getId(), 0, 10, "id", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
        assertEquals(requester.getId(), response.content().iterator().next().requesterId());
    }

    @Test
    public void find_issue_responses_by_requester_id_and_created_date_range_test() {
        insertResponse("in range");

        val response = responseTools.findIssueResponsesByRequesterIdAndCreatedDateRange(
                requester.getId(), CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void find_issue_responses_outside_created_date_range_throws_test() {
        insertResponse("too old for the window");

        val longAgo = Instant.now().minus(400, ChronoUnit.DAYS).toString();
        val slightlyLessLongAgo = Instant.now().minus(300, ChronoUnit.DAYS).toString();

        assertThrows(
                ResponseStatusException.class,
                () -> responseTools.findIssueResponsesByCreatedDateRange(
                        slightlyLessLongAgo, longAgo, 0, 10, "created", SortDirection.Ascending)
        );
    }

    @Test
    public void save_issue_response_creates_new_record_test() {
        val response = responseTools.saveIssueResponse(new IssueResponseToolRequest(
                null, request.getId(), requester.getId(), "created through the tool"));

        assertNotNull(response.id());
        assertEquals(request.getId(), response.requestId());
        assertEquals("created through the tool", response.responseBody());
    }

    @Test
    public void save_issue_response_updates_existing_record_test() {
        val saved = insertResponse("original body");

        val response = responseTools.saveIssueResponse(new IssueResponseToolRequest(
                saved.getId(), request.getId(), requester.getId(), "updated body"));

        assertEquals(saved.getId(), response.id());
        assertEquals("updated body", response.responseBody());
    }

    @Test
    public void save_issue_response_with_unknown_id_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> responseTools.saveIssueResponse(new IssueResponseToolRequest(
                        999999, request.getId(), requester.getId(), "ghost"))
        );
    }

    @Test
    public void hard_delete_issue_response_test() {
        val saved = insertResponse("delete me");

        val deleted = responseTools.hardDeleteIssueResponse(saved.getId());

        assertEquals(saved.getId(), deleted.id());
        assertThrows(
                ResponseStatusException.class,
                () -> responseTools.findIssueResponseById(saved.getId())
        );
    }

    @Test
    public void hard_delete_unknown_issue_response_throws_test() {
        assertThrows(ResponseStatusException.class, () -> responseTools.hardDeleteIssueResponse(999999));
    }
}
