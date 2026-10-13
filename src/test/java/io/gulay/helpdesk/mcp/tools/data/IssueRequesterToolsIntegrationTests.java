package io.gulay.helpdesk.mcp.tools.data;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.util.SortDirection;
import io.gulay.helpdesk.mcp.dto.IssueRequesterToolRequest;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

public class IssueRequesterToolsIntegrationTests extends TestBase {

    private static final String CREATED_AFTER = Instant.now().minus(1, ChronoUnit.DAYS).toString();
    private static final String CREATED_BEFORE = Instant.now().plus(1, ChronoUnit.DAYS).toString();

    @Autowired
    private IssueRequesterTools requesterTools;

    @BeforeEach
    public void setup() {
        issueRequesterService.hardDeleteAll();
    }

    private IssueRequesterModel insertRequester(String fullName, String email, boolean isActive) {
        return issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName(fullName)
                .email(email)
                .isActive(isActive)
                .build());
    }

    @Test
    public void find_issue_requester_by_id_maps_all_fields_test() {
        val saved = insertRequester("tool_full_name", "tool@email.com", true);

        val response = requesterTools.findIssueRequesterById(saved.getId());

        assertEquals(saved.getId(), response.id());
        assertEquals("tool_full_name", response.fullName());
        assertEquals("tool@email.com", response.email());
        assertTrue(response.isActive());
        assertNotNull(response.created());
        assertDoesNotThrow(() -> Instant.parse(response.created()));
    }

    @Test
    public void find_issue_requester_by_unknown_id_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.findIssueRequesterById(999999)
        );
    }

    @Test
    public void find_issue_requester_by_id_and_active_status_test() {
        val saved = insertRequester("active_requester", "active@email.com", true);

        val response = requesterTools.findIssueRequesterByIdAndActiveStatus(saved.getId(), true);

        assertEquals(saved.getId(), response.id());
    }

    @Test
    public void find_issue_requester_by_id_and_mismatched_active_status_throws_test() {
        val saved = insertRequester("active_requester", "active2@email.com", true);

        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.findIssueRequesterByIdAndActiveStatus(saved.getId(), false)
        );
    }

    @Test
    public void find_all_issue_requesters_returns_paged_response_test() {
        insertRequester("paged_one", "paged1@email.com", true);
        insertRequester("paged_two", "paged2@email.com", true);

        val response = requesterTools.findAllIssueRequesters(0, 10, "id", SortDirection.Ascending);

        assertEquals(2, response.totalElements());
        assertEquals(2, response.numberOfElements());
        assertEquals(1, response.totalPages());
        assertEquals(2, response.content().size());
    }

    @Test
    public void find_all_issue_requesters_honours_page_size_test() {
        insertRequester("paged_one", "paged1@email.com", true);
        insertRequester("paged_two", "paged2@email.com", true);

        val response = requesterTools.findAllIssueRequesters(0, 1, "id", SortDirection.Ascending);

        assertEquals(2, response.totalElements());
        assertEquals(1, response.numberOfElements());
        assertEquals(2, response.totalPages());
    }

    @Test
    public void find_all_issue_requesters_honours_sort_direction_test() {
        val first = insertRequester("sort_one", "sort1@email.com", true);
        val second = insertRequester("sort_two", "sort2@email.com", true);

        val ascending = requesterTools.findAllIssueRequesters(0, 10, "id", SortDirection.Ascending);
        val descending = requesterTools.findAllIssueRequesters(0, 10, "id", SortDirection.Descending);

        assertEquals(first.getId(), ascending.content().iterator().next().id());
        assertEquals(second.getId(), descending.content().iterator().next().id());
    }

    @Test
    public void find_all_issue_requesters_on_empty_table_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.findAllIssueRequesters(0, 10, "id", SortDirection.Ascending)
        );
    }

    @Test
    public void find_issue_requesters_by_full_name_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);
        insertRequester("Grace Hopper", "grace@email.com", true);

        val response = requesterTools.findIssueRequestersByFullName(
                "lovelace", 0, 10, "id", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
        assertEquals("Ada Lovelace", response.content().iterator().next().fullName());
    }

    @Test
    public void find_issue_requesters_by_email_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);
        insertRequester("Grace Hopper", "grace@email.com", true);

        val response = requesterTools.findIssueRequestersByEmail(
                "GRACE@", 0, 10, "id", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
        assertEquals("grace@email.com", response.content().iterator().next().email());
    }

    @Test
    public void find_issue_requesters_by_full_name_and_created_date_range_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);

        val response = requesterTools.findIssueRequestersByFullNameAndCreatedDateRange(
                "ada", CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void find_issue_requesters_by_email_and_created_date_range_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);

        val response = requesterTools.findIssueRequestersByEmailAndCreatedDateRange(
                "ada@", CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, response.totalElements());
    }

    @Test
    public void find_issue_requesters_by_created_date_range_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);
        insertRequester("Grace Hopper", "grace@email.com", false);

        val response = requesterTools.findIssueRequestersByCreatedDateRange(
                CREATED_BEFORE, CREATED_AFTER, 0, 10, "created", SortDirection.Descending);

        assertEquals(2, response.totalElements());
    }

    @Test
    public void find_issue_requesters_by_created_date_range_and_active_status_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);
        insertRequester("Grace Hopper", "grace@email.com", false);

        val active = requesterTools.findIssueRequestersByCreatedDateRangeAndActiveStatus(
                CREATED_BEFORE, CREATED_AFTER, true, 0, 10, "created", SortDirection.Ascending);

        assertEquals(1, active.totalElements());
        assertTrue(active.content().iterator().next().isActive());
    }

    @Test
    public void find_issue_requesters_outside_created_date_range_throws_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);

        val longAgo = Instant.now().minus(400, ChronoUnit.DAYS).toString();
        val slightlyLessLongAgo = Instant.now().minus(300, ChronoUnit.DAYS).toString();

        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.findIssueRequestersByCreatedDateRange(
                        slightlyLessLongAgo, longAgo, 0, 10, "created", SortDirection.Ascending)
        );
    }

    @Test
    public void find_issue_requesters_with_invalid_date_format_throws_test() {
        insertRequester("Ada Lovelace", "ada@email.com", true);

        assertThrows(
                java.time.format.DateTimeParseException.class,
                () -> requesterTools.findIssueRequestersByCreatedDateRange(
                        "09-07-2026", CREATED_AFTER, 0, 10, "created", SortDirection.Ascending)
        );
    }

    @Test
    public void issue_requester_exists_and_active_test() {
        val saved = insertRequester("Ada Lovelace", "ada@email.com", true);

        assertTrue(requesterTools.issueRequesterExistsAndActive(saved.getId(), true));
        assertFalse(requesterTools.issueRequesterExistsAndActive(saved.getId(), false));
        assertFalse(requesterTools.issueRequesterExistsAndActive(999999, true));
    }

    @Test
    public void toggle_issue_requester_activation_test() {
        val saved = insertRequester("Ada Lovelace", "ada@email.com", true);

        val toggled = requesterTools.toggleIssueRequesterActivation(saved.getId());
        assertFalse(toggled.isActive());

        val toggledBack = requesterTools.toggleIssueRequesterActivation(saved.getId());
        assertTrue(toggledBack.isActive());
    }

    @Test
    public void save_issue_requester_creates_new_record_test() {
        val response = requesterTools.saveIssueRequester(new IssueRequesterToolRequest(
                null, "Created By Tool", "tool-created@email.com", true));

        assertNotNull(response.id());
        assertEquals("Created By Tool", response.fullName());
        assertEquals(response.id(), requesterTools.findIssueRequesterById(response.id()).id());
    }

    @Test
    public void save_issue_requester_updates_existing_record_test() {
        val saved = insertRequester("Original Name", "original@email.com", true);

        val response = requesterTools.saveIssueRequester(new IssueRequesterToolRequest(
                saved.getId(), "Updated Name", "updated@email.com", false));

        assertEquals(saved.getId(), response.id());
        assertEquals("Updated Name", response.fullName());
        assertFalse(response.isActive());
    }

    @Test
    public void save_issue_requester_with_duplicate_email_throws_test() {
        insertRequester("Ada Lovelace", "duplicate@email.com", true);

        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.saveIssueRequester(new IssueRequesterToolRequest(
                        null, "Someone Else", "duplicate@email.com", true))
        );
    }

    @Test
    public void save_issue_requester_with_unknown_id_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.saveIssueRequester(new IssueRequesterToolRequest(
                        999999, "Ghost", "ghost@email.com", true))
        );
    }

    @Test
    public void hard_delete_issue_requester_test() {
        val saved = insertRequester("Ada Lovelace", "ada@email.com", true);

        val deleted = requesterTools.hardDeleteIssueRequester(saved.getId());

        assertEquals(saved.getId(), deleted.id());
        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.findIssueRequesterById(saved.getId())
        );
    }

    @Test
    public void hard_delete_unknown_issue_requester_throws_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> requesterTools.hardDeleteIssueRequester(999999)
        );
    }
}
