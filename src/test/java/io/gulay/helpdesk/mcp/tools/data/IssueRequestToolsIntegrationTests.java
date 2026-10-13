package io.gulay.helpdesk.mcp.tools.data;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.util.SortDirection;
import io.gulay.helpdesk.mcp.dto.IssueRequestToolRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class IssueRequestToolsIntegrationTests extends TestBase {

    private static final String CREATED_AFTER = Instant.now().minus(1, ChronoUnit.DAYS).toString();
    private static final String CREATED_BEFORE = Instant.now().plus(1, ChronoUnit.DAYS).toString();

    @Autowired
    private IssueRequestTools requestTools;

    private IssueRequesterModel requester;

    @BeforeEach
    public void setup() {
        issueResponseService.hardDeleteAll();
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();

        requester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("request_tools_requester")
                .isActive(true)
                .email(UUID.randomUUID() + "@email.com")
                .build());
    }

    private IssueRequestModel insertRequest(String body) {
        return issueRequestService.save(IssueRequestModel
                .builder()
                .requester(requester)
                .isSolved(false)
                .body(body)
                .build());
    }

    @Test
    public void find_issue_request_by_id_maps_all_fields_test() {
        val saved = insertRequest("Printer is offline");

        val response = requestTools.findIssueRequestById(saved.getId());

        assertEquals(saved.getId(), response.id());
        assertEquals(requester.getId(), response.requesterId());
        assertEquals("Printer is offline", response.requestBody());
        assertFalse(response.isSolved());
        assertNotNull(response.created());
        assertNull(response.solved());
        assertDoesNotThrow(() -> Instant.parse(response.created()));
    }

    @Test
    public void find_issue_request_by_unknown_id_throws_test() {
        assertThrows(ResponseStatusException.class, () -> requestTools.findIssueRequestById(999999));
    }

    @Test
    public void find_issue_request_by_id_and_solved_status_test() {
        val saved = insertRequest("Unsolved issue");

        val response = requestTools.findIssueRequestByIdAndSolvedStatus(saved.getId(), false);
        assertEquals(saved.getId(), response.id());

        assertThrows(
                ResponseStatusException.class,
                () -> requestTools.findIssueRequestByIdAndSolvedStatus(saved.getId(), true)
        );
    }

    @Test
    public void find_all_issue_requests_returns_paged_response_test() {
        insertRequest("first issue");
        insertRequest("second issue");

        val response = requestTools.findAllIssueRequests(0, 10, "id", SortDirection.Ascending);

        assertEquals(2, response.totalElements());
        assertEquals(2, response.content().size());
        assertEquals(1, response.totalPages());
    }

    @Test
    public void find_all_issue_requests_on_empty_table_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> requestTools.findAllIssueRequests(0, 10, "id", SortDirection.Ascending)
        );
    }

    @Test
    public void find_issue_requests_by_created_date_range_test() {
        insertRequest("in range");

        val response = requestTools.findIssueRequestsByCreatedDateRange(
                CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void find_issue_requests_by_created_date_range_and_solved_status_test() {
        val unsolved = insertRequest("still open");
        val solved = insertRequest("already handled");
        requestTools.solveIssueRequest(solved.getId());

        val openOnes = requestTools.findIssueRequestsByCreatedDateRangeAndSolvedStatus(
                CREATED_BEFORE, CREATED_AFTER, false, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, openOnes.totalElements());
        assertEquals(unsolved.getId(), openOnes.content().iterator().next().id());

        val solvedOnes = requestTools.findIssueRequestsByCreatedDateRangeAndSolvedStatus(
                CREATED_BEFORE, CREATED_AFTER, true, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, solvedOnes.totalElements());
        assertEquals(solved.getId(), solvedOnes.content().iterator().next().id());
    }

    @Test
    public void find_issue_requests_by_solved_date_range_test() {
        val saved = insertRequest("to be solved");
        requestTools.solveIssueRequest(saved.getId());

        val response = requestTools.findIssueRequestsBySolvedDateRange(
                CREATED_BEFORE, CREATED_AFTER, 0, 10, "solved", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
        assertNotNull(response.content().iterator().next().solved());
    }

    @Test
    public void find_issue_requests_by_requester_id_test() {
        insertRequest("first issue");
        insertRequest("second issue");

        val response = requestTools.findIssueRequestsByRequesterId(
                requester.getId(), 0, 10, "id", SortDirection.Ascending);

        assertEquals(2, response.totalElements());
        assertTrue(response.content().stream()
                .allMatch(item -> item.requesterId().equals(requester.getId())));
    }

    @Test
    public void find_issue_requests_by_unknown_requester_id_throws_test() {
        insertRequest("first issue");

        assertThrows(
                ResponseStatusException.class,
                () -> requestTools.findIssueRequestsByRequesterId(
                        999999, 0, 10, "id", SortDirection.Ascending)
        );
    }

    @Test
    public void find_issue_requests_by_requester_id_and_created_date_range_test() {
        insertRequest("in range");

        val response = requestTools.findIssueRequestsByRequesterIdAndCreatedDateRange(
                requester.getId(), CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void solve_issue_request_sets_solved_flag_and_timestamp_test() {
        val saved = insertRequest("please solve me");

        val solved = requestTools.solveIssueRequest(saved.getId());

        assertTrue(solved.isSolved());
        assertNotNull(solved.solved());
        assertDoesNotThrow(() -> Instant.parse(solved.solved()));
    }

    @Test
    public void solve_unknown_issue_request_throws_test() {
        assertThrows(ResponseStatusException.class, () -> requestTools.solveIssueRequest(999999));
    }

    @Test
    public void issue_request_exists_test() {
        val saved = insertRequest("exists");

        assertTrue(requestTools.issueRequestExists(saved.getId()));
        assertFalse(requestTools.issueRequestExists(999999));
    }

    @Test
    public void save_issue_request_creates_new_record_test() {
        val response = requestTools.saveIssueRequest(new IssueRequestToolRequest(
                null, requester.getId(), "created through the tool", false, null));

        assertNotNull(response.id());
        assertEquals(requester.getId(), response.requesterId());
        assertEquals("created through the tool", response.requestBody());
    }

    @Test
    public void save_issue_request_updates_existing_record_test() {
        val saved = insertRequest("original body");

        val response = requestTools.saveIssueRequest(new IssueRequestToolRequest(
                saved.getId(), requester.getId(), "updated body", false, null));

        assertEquals(saved.getId(), response.id());
        assertEquals("updated body", response.requestBody());
    }

    @Test
    public void save_issue_request_with_unknown_id_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> requestTools.saveIssueRequest(new IssueRequestToolRequest(
                        999999, requester.getId(), "ghost", false, null))
        );
    }

    @Test
    public void hard_delete_issue_request_test() {
        val saved = insertRequest("delete me");

        val deleted = requestTools.hardDeleteIssueRequest(saved.getId());

        assertEquals(saved.getId(), deleted.id());
        assertFalse(requestTools.issueRequestExists(saved.getId()));
    }

    @Test
    public void hard_delete_unknown_issue_request_throws_test() {
        assertThrows(ResponseStatusException.class, () -> requestTools.hardDeleteIssueRequest(999999));
    }
}
