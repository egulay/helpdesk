package com.helpdesk.data;

import com.helpdesk.TestBase;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.model.IssueResponseModel;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

public class IssueResponseServiceIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester;

    private static IssueRequestModel newIssueRequest;

    private static IssueResponseModel newIssueResponse1;
    private static IssueResponseModel newIssueResponse2;

    public void insertNewIssueRequester() {
        newIssueRequester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test1_full_name")
                .isActive(true)
                .email(UUID.randomUUID().toString().concat("@email.com"))
                .build());
    }

    public void insertNewIssueRequest() {
        newIssueRequest = issueRequestService.save(IssueRequestModel
                .builder()
                .isSolved(false)
                .requester(newIssueRequester)
                .body("Body Text 1")
                .build());
    }

    public void insertNewIssueResponse1() {
        newIssueResponse1 = issueResponseService.save(IssueResponseModel
                .builder()
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("Response body 1")
                .build());
    }

    public void insertNewIssueResponse2() {
        newIssueResponse2 = issueResponseService.save(IssueResponseModel
                .builder()
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("Response body 2")
                .build());
    }

    public void testCollection(GenericPagedModel<IssueResponseModel> model) {
        assertFalse(model.getContent().isEmpty());

        assertTrue(model.getContent()
                .stream()
                .anyMatch(f -> f.getId().equals(newIssueResponse1.getId())));

        assertTrue(model.getContent()
                .stream()
                .anyMatch(f -> f.getId().equals(newIssueResponse2.getId())));
    }

    @Before
    public void setup() {
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();
        issueResponseService.hardDeleteAll();
    }

    @Test
    public void insert_issue_response_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        assertNotNull(newIssueResponse1);
    }

    @Test(expected = ResponseStatusException.class)
    public void insert_issue_response_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();

        issueResponseService.save(IssueResponseModel
                .builder()
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("  ")
                .build());
    }

    @Test
    public void update_issue_response_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val updated = issueResponseService.save(IssueResponseModel
                .builder()
                .id(newIssueResponse1.getId())
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("some new body")
                .build());

        assertEquals(newIssueResponse1.getId(), updated.getId());
        assertNotEquals(newIssueResponse1.getBody(), updated.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void update_issue_response_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        issueResponseService.save(IssueResponseModel
                .builder()
                .id(newIssueResponse1.getId())
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("        ")
                .build());
    }

    @Test
    public void find_issue_response_by_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val found = issueResponseService.findById(newIssueResponse1.getId());

        assertNotNull(found);
        assertEquals(newIssueResponse1.getId(), found.getId());
        assertEquals(newIssueResponse1.getBody(), found.getBody());
    }

    @Test(expected = ResponseStatusException.class)
    public void find_issue_response_by_id_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        issueResponseService.findById(-1);
    }

    @Test
    public void find_all_issue_responses_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        testCollection(issueResponseService.findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_with_exception_test() {
        testCollection(issueResponseService.findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueResponseService
                .findAllByCreatedBeforeAndCreatedAfter(tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_by_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueResponseService
                .findAllByCreatedBeforeAndCreatedAfter(tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_request_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        testCollection(issueResponseService
                .findAllByRequestId(newIssueRequest.getId(), 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_by_request_id_with_exception_test() {
        testCollection(issueResponseService
                .findAllByRequestId(-1, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_request_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueResponseService
                .findAllByRequestIdAndCreatedBeforeAndCreatedAfter(newIssueRequest.getId(),
                        tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_by_request_id_and_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueResponseService
                .findAllByRequestIdAndCreatedBeforeAndCreatedAfter(-1,
                        tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_requester_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        testCollection(issueResponseService
                .findAllByRequesterId(newIssueRequester.getId(), 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_by_requester_id_with_exception_test() {
        testCollection(issueResponseService
                .findAllByRequesterId(-1, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_requester_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueResponseService
                .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(newIssueRequester.getId(),
                        tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void find_all_issue_responses_by_requester_id_and_created_before_and_created_after_with_exception_test() {
        val yesterday = new Date(Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        val tomorrow = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());

        testCollection(issueResponseService
                .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(-1,
                        tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test(expected = ResponseStatusException.class)
    public void delete_issue_response_test() {
        insertNewIssueResponse1();

        val deleted = issueResponseService.hardDelete(newIssueResponse1.getId());

        assertEquals(newIssueResponse1.getId(), deleted.getId());

        issueRequesterService.findById(deleted.getId());
    }

}
