package io.gulay.helpdesk.data;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.model.IssueResponseModel;
import io.gulay.helpdesk.data.util.GenericPagedModel;
import io.gulay.helpdesk.data.util.SortDirection;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


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

    @BeforeEach
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

    @Test
    public void insert_issue_response_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();

        assertThrows(ResponseStatusException.class, () -> issueResponseService.save(IssueResponseModel
                .builder()
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("  ")
                .build()));
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

    @Test
    public void update_issue_response_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        assertThrows(ResponseStatusException.class, () -> issueResponseService.save(IssueResponseModel
                .builder()
                .id(newIssueResponse1.getId())
                .requester(newIssueRequester)
                .request(newIssueRequest)
                .body("        ")
                .build()));
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

    @Test
    public void find_issue_response_by_id_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        assertThrows(ResponseStatusException.class, () -> issueResponseService.findById(-1));
    }

    @Test
    public void find_all_issue_responses_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        testCollection(issueResponseService.findAll(0, 10, "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_with_exception_test() {
        assertThrows(ResponseStatusException.class, () ->
                testCollection(issueResponseService.findAll(0, 10, "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_responses_by_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueResponseService
                .findAllByCreatedBeforeAndCreatedAfter(tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_created_before_and_created_after_with_exception_test() {
        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        assertThrows(ResponseStatusException.class, () ->
                testCollection(issueResponseService
                        .findAllByCreatedBeforeAndCreatedAfter(tomorrow, yesterday, 0, 10,
                                "created", SortDirection.Descending)));
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

    @Test
    public void find_all_issue_responses_by_request_id_with_exception_test() {
        assertThrows(ResponseStatusException.class, () ->
                testCollection(issueResponseService
                        .findAllByRequestId(-1, 0, 10,
                                "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_responses_by_request_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueResponseService
                .findAllByRequestIdAndCreatedBeforeAndCreatedAfter(newIssueRequest.getId(),
                        tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_request_id_and_created_before_and_created_after_with_exception_test() {
        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        assertThrows(ResponseStatusException.class, () ->
                testCollection(issueResponseService
                        .findAllByRequestIdAndCreatedBeforeAndCreatedAfter(-1,
                                tomorrow, yesterday, 0, 10,
                                "created", SortDirection.Descending)));
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

    @Test
    public void find_all_issue_responses_by_requester_id_with_exception_test() {
        assertThrows(ResponseStatusException.class, () ->
                testCollection(issueResponseService
                        .findAllByRequesterId(-1, 0, 10,
                                "created", SortDirection.Descending)));
    }

    @Test
    public void find_all_issue_responses_by_requester_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        testCollection(issueResponseService
                .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(newIssueRequester.getId(),
                        tomorrow, yesterday, 0, 10,
                        "created", SortDirection.Descending));
    }

    @Test
    public void find_all_issue_responses_by_requester_id_and_created_before_and_created_after_with_exception_test() {
        val yesterday = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        val tomorrow = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        assertThrows(ResponseStatusException.class, () ->
                testCollection(issueResponseService
                        .findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(-1,
                                tomorrow, yesterday, 0, 10,
                                "created", SortDirection.Descending)));
    }

}
