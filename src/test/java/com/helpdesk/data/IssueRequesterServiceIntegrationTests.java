package com.helpdesk.data;

import com.helpdesk.TestBase;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.Assert.*;

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

    @Before
    public void setup() {
        issueRequesterService.hardDeleteAll();
    }

    @Test
    public void insert_issue_requester_test() {
        insertNewIssueRequester1();
        assertNotNull(newIssueRequester1);
    }

    @Test(expected = ResponseStatusException.class)
    public void insert_issue_requester_with_exception_test() {
        newIssueRequester2 = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test2_full_name")
                .email("test2 email")
                .build());
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

    @Test(expected = ResponseStatusException.class)
    public void update_issue_requester_with_exception_test() {
        insertNewIssueRequester1();

        issueRequesterService.save(IssueRequesterModel
                .builder()
                .id(newIssueRequester1.getId())
                .fullName("   ")
                .email("new@email.net")
                .build());
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

    @Test(expected = ResponseStatusException.class)
    public void find_issue_requester_by_id_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        issueRequesterService.findById(-1);
    }

    @Test
    public void find_all_issue_requesters_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_with_exception_test() {
        testCollection(issueRequesterService
                .findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_full_name_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCase("ull_na", 0, 10, "created",
                        SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_by_full_name_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCase("oh noes!!", 0, 10, "created",
                        SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_full_name_and_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("ull_na",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_by_full_name_and_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("ull_na",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_email_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAllByEmailContainingIgnoreCase("@email.com", 0, 10, "created",
                        SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_by_email_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        testCollection(issueRequesterService
                .findAllByFullNameContainingIgnoreCase("oh noes!!", 0, 10, "created",
                        SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_email_and_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("@email.com",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_by_email_and_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter("ull_na",
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_created_before_and_created_after_and_is_active_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                        tomorrow, yesterday, true, 0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_by_created_before_and_created_after_and_is_active_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                        tomorrow, yesterday, false, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_requesters_by_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfter(
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_requesters_by_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequesterService
                .findAllByCreatedBeforeAndCreatedAfter(
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void toggle_issue_requester_activation_test() {
        insertNewIssueRequester1();

        val toggled = issueRequesterService.toggleActivation(newIssueRequester1.getId());

        assertEquals(newIssueRequester1.getId(), toggled.getId());
        assertNotEquals(newIssueRequester1.getIsActive(), toggled.getIsActive());
    }

    @Test(expected = ResponseStatusException.class)
    public void delete_issue_requester_test() {
        insertNewIssueRequester1();

        val deleted = issueRequesterService.hardDelete(newIssueRequester1.getId());

        assertEquals(newIssueRequester1.getId(), deleted.getId());

        issueRequesterService.findById(deleted.getId());
    }
}
