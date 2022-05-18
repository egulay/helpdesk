package com.helpdesk.controller;

import com.helpdesk.IssueRequest;
import com.helpdesk.PagedData;
import com.helpdesk.RestConfiguration;
import com.helpdesk.TestBase;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class IssueRequestControllerIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester;

    private static IssueRequestModel newIssueRequest1;
    private static IssueRequestModel newIssueRequest2;

    public void insertNewIssueRequester() {
        newIssueRequester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test1_full_name")
                .email(UUID.randomUUID().toString().concat("@email.com"))
                .build());
    }

    public void insertNewIssueRequest1() {
        newIssueRequest1 = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(newIssueRequester)
                .body("Body Text 1")
                .build());
    }

    public void insertNewIssueRequest2() {
        newIssueRequest2 = issueRequestService.save(IssueRequestModel
                .builder()
                .requester(newIssueRequester)
                .body("Body Text 2")
                .build());
    }

    public void testPagedDataResponse(PagedData model) {
        assertFalse(model.getIssueRequests().getIssueRequestsList().isEmpty());
        assertEquals(2, model.getTotalElements());
        assertEquals(1, model.getTotalPages());
        assertEquals(2, model.getNumberOfElements());

        assertTrue(model.getIssueRequests().getIssueRequestsList()
                .stream()
                .anyMatch(f -> String.valueOf(f.getId()).equals(newIssueRequest1.getId().toString())));

        assertTrue(model.getIssueRequests().getIssueRequestsList()
                .stream()
                .anyMatch(f -> String.valueOf(f.getId()).equals(newIssueRequest2.getId().toString())));
    }

    @Before
    public void setup() {
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();
    }

    @Test
    public void get_issue_request_by_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/")
                .concat(newIssueRequest1.getId().toString());

        val response = restTemplate.getForEntity(url, IssueRequest.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueRequest1.getId()), String.valueOf(response.getBody().getId()));
        assertEquals(newIssueRequest1.getBody(), response.getBody().getBody());
        assertEquals(String.valueOf(newIssueRequest1.getRequester().getId()),
                String.valueOf(response.getBody().getRequesterId()));
    }

    @Test
    public void get_issue_request_by_id_with_exception_test() {
        val id = 1;
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/")
                .concat(String.valueOf(1));

        try {
            restTemplate.getForEntity(url, IssueRequest.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(id)));
        }
    }

    @Test
    public void get_all_issue_requests_by_requester_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("/").concat(String.valueOf(newIssueRequester.getId()))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_requests_by_requester_id_and_created_before_and_created_after_with_exception_test() {
        val id = 1;
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("/").concat(String.valueOf(id))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
            assertThat(ex.getMessage(), containsString(String.valueOf(id)));
        }
    }

    @Test
    public void get_all_issue_requests_by_requester_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("/").concat(String.valueOf(newIssueRequester.getId()));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_requests_by_requester_id_with_exception_test() {
        insertNewIssueRequester();
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("/").concat(String.valueOf(newIssueRequester.getId()));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(newIssueRequester.getId())));
        }
    }

    @Test
    public void get_all_issue_requests_by_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_requests_by_created_before_and_created_after_with_exception_test() {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
        }
    }


    @Test
    public void get_all_issue_requests_by_is_solved_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        issueRequestService.solveIssue(newIssueRequest1.getId());
        issueRequestService.solveIssue(newIssueRequest2.getId());

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all_solved")
                .concat("/").concat(String.valueOf(true))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_requests_by_is_solved_and_created_before_and_created_after_with_exception_test() {
        val isSolved = true;
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all_solved")
                .concat("/").concat(String.valueOf(isSolved))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
            assertThat(ex.getMessage(), containsString(String.valueOf(isSolved)));
        }
    }

    @Test
    public void get_all_issue_requests_by_solved_before_and_solved_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        insertNewIssueRequest2();

        issueRequestService.solveIssue(newIssueRequest1.getId());
        issueRequestService.solveIssue(newIssueRequest2.getId());

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all_solved")
                .concat("?solvedBefore=").concat(String.valueOf(tomorrow))
                .concat("&solvedAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_requests_by_solved_before_and_solved_after_with_exception_test() {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all_solved")
                .concat("?solvedBefore=").concat(String.valueOf(tomorrow))
                .concat("&solvedAfter=").concat(String.valueOf(yesterday));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
        }
    }

    @Test
    public void solve_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/solve/")
                .concat(String.valueOf(newIssueRequest1.getId()));

        val response = restTemplate.exchange(url,
                HttpMethod.PUT, null, IssueRequest.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueRequest1.getId()), String.valueOf(response.getBody().getId()));
        assertNotEquals(newIssueRequest1.getIsSolved(), response.getBody().getIsSolved().getData());
    }

    @Test
    public void delete_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/delete/")
                .concat(newIssueRequest1.getId().toString());

        val response = restTemplate.exchange(url,
                HttpMethod.DELETE, null, IssueRequest.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());
        assertEquals(String.valueOf(response.getBody().getId()), (newIssueRequest1.getId().toString()));

        try {
            issueRequesterService.findById(response.getBody().getId());
        } catch (final ResponseStatusException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(response.getBody().getId())));
        }
    }

    @Test
    public void insert_issue_request_test() {
        insertNewIssueRequester();

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setBody("Some request Body")
                .setRequesterId(newIssueRequester.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        val response = restTemplate.postForEntity(url,
                new HttpEntity<>(issueRequestToPost), IssueRequest.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        val found = issueRequestService.findById(response.getBody().getId());

        assertNotNull(found);

        assertEquals(String.valueOf(response.getBody().getId()), found.getId().toString());
        assertEquals(response.getBody().getBody(), found.getBody());
    }

    @Test
    public void insert_issue_request__with_exception_test() {
        insertNewIssueRequester();

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setBody("    ")
                .setRequesterId(newIssueRequester.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueRequestToPost), IssueRequest.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString("must not be empty"));
        }
    }

    @Test
    public void update_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setId(newIssueRequest1.getId())
                .setRequesterId(newIssueRequester.getId())
                .setBody("Hebee bidi ekeeke dada")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        val response = restTemplate.postForEntity(url,
                new HttpEntity<>(issueRequestToPost), IssueRequest.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(response.getBody().getId()), newIssueRequest1.getId().toString());
        assertNotEquals(response.getBody().getBody(), newIssueRequest1.getBody());
    }

    @Test
    public void update_issue_request_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setId(newIssueRequest1.getId())
                .setRequesterId(newIssueRequester.getId())
                .setBody("    ")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueRequestToPost), IssueRequest.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString("must not be empty"));
        }
    }
}
