package com.helpdesk.data;

import com.helpdesk.TestBase;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.util.SortDirection;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class IssueRequestServiceIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester;

    private static IssueRequestModel newIssueRequest1;
    private static IssueRequestModel newIssueRequest2;

    public void insertNewIssueRequester() {
        newIssueRequester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test1_full_name")
                .isActive(true)
                .email(UUID.randomUUID().toString().concat("@email.com"))
                .build());
    }

    public void insertNewIssueRequest1() {
        newIssueRequest1 = issueRequestService.save(IssueRequestModel
                .builder()
                .isSolved(false)
                .requester(newIssueRequester)
                .body("Body Text 1")
                .build());
    }

    public void insertNewIssueRequest2() {
        newIssueRequest2 = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(newIssueRequester)
                .isSolved(false)
                .body("Body Text 2")
                .build());
    }

    public void testCollection(GenericPagedModel<IssueRequestModel> model) {
        assertFalse(model.getContent().isEmpty());

        assertTrue(model.getContent()
                .stream()
                .anyMatch(f -> f.getId().equals(newIssueRequest1.getId())));

        assertTrue(model.getContent()
                .stream()
                .anyMatch(f -> f.getId().equals(newIssueRequest2.getId())));
    }

    @Before
    public void setup() {
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();
    }

    @Test
    public void insert_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        assertNotNull(newIssueRequest1);
    }

    @Test(expected = ResponseStatusException.class)
    public void insert_issue_request_with_exception_test() {
        insertNewIssueRequester();

        newIssueRequest2 = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(newIssueRequester)
                .body("   ")
                .build());
    }

    @Test
    public void update_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val updated = issueRequestService.save(IssueRequestModel
                .builder()
                .isSolved(newIssueRequest1.getIsSolved())
                .id(newIssueRequest1.getId())
                .requester(newIssueRequester)
                .body("some new body")
                .build());

        assertEquals(newIssueRequest1.getId(), updated.getId());
        assertNotEquals(newIssueRequest1.getBody(), updated.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void update_issue_request_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        issueRequestService.save(IssueRequestModel
                .builder()
                .id(newIssueRequest1.getId())
                .requester(newIssueRequester)
                .body("      ")
                .build());
    }

    @Test
    public void find_issue_request_by_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val found = issueRequestService.findById(newIssueRequest1.getId());

        assertNotNull(found);
        assertEquals(newIssueRequest1.getId(), found.getId());
        assertEquals(newIssueRequest1.getBody(), found.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void find_issue_request_by_id_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        issueRequesterService.findById(-1);
    }

    @Test
    public void find_all_issue_requests_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        testCollection(issueRequestService.findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_with_exception_test() {
        testCollection(issueRequestService.findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_request_by_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllByCreatedBeforeAndCreatedAfter(tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_request_by_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllByCreatedBeforeAndCreatedAfter(tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_request_by_created_before_and_created_after_and_is_solved_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        issueRequestService.solveIssue(newIssueRequest1.getId());
        issueRequestService.solveIssue(newIssueRequest2.getId());

        val found1 = issueRequestService.findById(newIssueRequest1.getId());
        val found2 = issueRequestService.findById(newIssueRequest2.getId());

        assertTrue(found1.getIsSolved());
        assertTrue(found2.getIsSolved());

        assertNotNull(found1.getSolved());
        assertNotNull(found2.getSolved());

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllByCreatedBeforeAndCreatedAfterAndIsSolved(tomorrow, yesterday, true, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_request_by_created_before_and_created_after_and_is_solved_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllByCreatedBeforeAndCreatedAfterAndIsSolved(tomorrow, yesterday, true, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_request_by_solved_before_and_solved_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        issueRequestService.solveIssue(newIssueRequest1.getId());
        issueRequestService.solveIssue(newIssueRequest2.getId());

        val found1 = issueRequestService.findById(newIssueRequest1.getId());
        val found2 = issueRequestService.findById(newIssueRequest2.getId());

        assertTrue(found1.getIsSolved());
        assertTrue(found2.getIsSolved());

        assertNotNull(found1.getSolved());
        assertNotNull(found2.getSolved());

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllBySolvedBeforeAndSolvedAfter(tomorrow, yesterday, 0, 10,
                        "solved", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_request_by_solved_before_and_solved_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllBySolvedBeforeAndSolvedAfter(tomorrow, yesterday, 0, 10,
                        "solved", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_request_by_requester_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        testCollection(issueRequestService
                .findAllByRequesterId(newIssueRequester.getId(), 0, 10,
                        "solved", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_request_by_requester_id_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        testCollection(issueRequestService
                .findAllByRequesterId(-1, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_request_by_requester_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(newIssueRequester.getId(),
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_request_by_requester_id_and_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueRequestService
                .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(-1,
                        tomorrow, yesterday, 0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void solve_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        issueRequestService.solveIssue(newIssueRequest1.getId());
        issueRequestService.solveIssue(newIssueRequest2.getId());

        val found1 = issueRequestService.findById(newIssueRequest1.getId());
        val found2 = issueRequestService.findById(newIssueRequest2.getId());

        assertTrue(found1.getIsSolved());
        assertTrue(found2.getIsSolved());

        assertNotNull(found1.getSolved());
        assertNotNull(found2.getSolved());
    }

    @Test(expected = ResponseStatusException.class)
    public void delete_issue_requester_test() {
        insertNewIssueRequest1();

        val deleted = issueRequestService.hardDelete(newIssueRequest1.getId());

        assertEquals(newIssueRequest1.getId(), deleted.getId());

        issueRequesterService.findById(deleted.getId());
    }
}
