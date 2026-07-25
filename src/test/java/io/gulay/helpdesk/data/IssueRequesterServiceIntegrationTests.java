package io.gulay.helpdesk.data;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.util.GenericPagedModel;
import io.gulay.helpdesk.data.util.SortDirection;
import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;


public class IssueRequesterServiceIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester1;

    private static IssueRequesterModel newIssueRequester2;

    public void insertNewIssueRequester1() {
        newIssueRequester1 = issueRequesterService.save(IssueRequesterModel
                .builder()
                .isActive(true)
                .fullName("test1_full_name")
                .email("test1@email.com")
                .build());
    }

    public void insertNewIssueRequester2() {
        newIssueRequester2 = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test2_full_name")
                .isActive(true)
                .email("test2@email.com")
                .build());
    }

    public void testCollection(GenericPagedModel<IssueRequesterModel> model) {
        assertFalse(model.getContent().isEmpty());

        assertTrue(model.getContent()
                .stream()
                .anyMatch(f -> f.getId().equals(newIssueRequester1.getId())));

        assertTrue(model.getContent()
                .stream()
                .anyMatch(f -> f.getId().equals(newIssueRequester2.getId())));
    }

    @BeforeEach
    public void setup() {
        issueRequesterService.hardDeleteAll();
    }

    @Test
    public void insert_issue_requester_test() {
        insertNewIssueRequester1();
        assertNotNull(newIssueRequester1);
    }

    @Test
    public void insert_issue_requester_with_exception_test() {
        assertThrows(ResponseStatusException.class, () -> issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test2_full_name")
                .email("test2 email")
                .build()));
    }

    @Test
    public void update_issue_requester_test() {
        insertNewIssueRequester1();

        val updated = issueRequesterService.save(IssueRequesterModel
                .builder()
                .id(newIssueRequester1.getId())
                .isActive(true)
                .fullName("new full name")
                .email("new@email.net")
                .build());

        assertEquals(newIssueRequester1.getId(), updated.getId());
        assertNotEquals(newIssueRequester1.getFullName(), updated.getFullName());
        assertNotEquals(newIssueRequester1.getEmail(), updated.getEmail());
    }

    @Test
    public void update_issue_requester_with_exception_test() {
        insertNewIssueRequester1();

        assertThrows(ResponseStatusException.class, () -> issueRequesterService.save(IssueRequesterModel
                .builder()
                .id(newIssueRequester1.getId())
                .fullName("   ")
                .email("new@email.net")
                .build()));
    }

    @Test
    public void find_issue_requester_by_id_test() {
        insertNewIssueRequester1();

        val found = issueRequesterService.findById(newIssueRequester1.getId());

        assertNotNull(found);
        assertEquals(newIssueRequester1.getId(), found.getId());
        assertEquals(newIssueRequester1.getFullName(), found.getFullName());
        assertEquals(newIssueRequester1.getEmail(), found.getEmail());
    }

    @Test
    public void find_issue_requester_by_id_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        assertThrows(ResponseStatusException.class, () -> issueRequesterService.findById(-1));
    }

    @Test
    public void find_all_issue_requesters_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_with_exception_test() {
        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAll(0, 10, "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_requesters_by_full_name_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCase("ull_na", 0, 10, "created",
                        SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_full_name_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCase("oh noes!!", 0, 10, "created",
                        SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_requesters_by_full_name_and_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("ull_na",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_full_name_and_created_before_and_created_after_with_exception_test() {
        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("ull_na",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_requesters_by_email_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAllByEmailContainingIgnoreCase("@email.com", 0, 10, "created",
                        SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_email_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCase("oh noes!!", 0, 10, "created",
                        SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_requesters_by_email_and_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueRequesterService
                .findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("@email.com",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_email_and_created_before_and_created_after_with_exception_test() {
        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);


        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("ull_na",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_requesters_by_created_before_and_created_after_and_is_active_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                        tomorrow, yesterday, true, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_created_before_and_created_after_and_is_active_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                        tomorrow, yesterday, false, 0, 10, "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_requesters_by_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfter(
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_created_before_and_created_after_with_exception_test() {
        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        assertThrows(ResponseStatusException.class, () -> testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfter(
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending)));
    }

    @Test
    public void toggle_issue_requester_activation_test() {
        insertNewIssueRequester1();

        val toggled = issueRequesterService.toggleActivation(newIssueRequester1.getId());

        assertEquals(newIssueRequester1.getId(), toggled.getId());
        assertNotEquals(newIssueRequester1.getIsActive(), toggled.getIsActive());
    }
}
