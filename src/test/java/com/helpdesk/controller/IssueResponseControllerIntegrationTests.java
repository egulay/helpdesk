package com.helpdesk.controller;

import com.helpdesk.*;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.model.IssueResponseModel;
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

public class IssueResponseControllerIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester;

    private static IssueRequestModel newIssueRequest;

    private static IssueResponseModel newIssueResponse1;
    private static IssueResponseModel newIssueResponse2;

    public void insertNewIssueRequester() {
        newIssueRequester = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test1_full_name")
                .email(UUID.randomUUID().toString().concat("@email.com"))
                .build());
    }

    public void insertNewIssueRequest() {
        newIssueRequest = issueRequestService.save(IssueRequestModel
                .builder()
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

    public void testPagedDataResponse(PagedData model) {
        assertFalse(model.getIssueResponses().getIssueResponsesList().isEmpty());
        assertEquals(2, model.getTotalElements());
        assertEquals(1, model.getTotalPages());
        assertEquals(2, model.getNumberOfElements());

        assertTrue(model.getIssueResponses().getIssueResponsesList()
                .stream()
                .anyMatch(f -> String.valueOf(f.getId()).equals(newIssueResponse1.getId().toString())));

        assertTrue(model.getIssueResponses().getIssueResponsesList()
                .stream()
                .anyMatch(f -> String.valueOf(f.getId()).equals(newIssueResponse2.getId().toString())));
    }

    @Before
    public void setup() {
        issueRequestService.hardDeleteAll();
        issueRequesterService.hardDeleteAll();
        issueResponseService.hardDeleteAll();
    }

    @Test
    public void get_issue_response_by_id_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/")
                .concat(newIssueResponse1.getId().toString());

        val response = restTemplate.getForEntity(url, IssueResponse.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueResponse1.getId()), String.valueOf(response.getBody().getId()));
        assertEquals(newIssueResponse1.getBody(), response.getBody().getBody());
        assertEquals(String.valueOf(newIssueResponse1.getRequester().getId()),
                String.valueOf(response.getBody().getRequesterId()));
        assertEquals(String.valueOf(newIssueResponse1.getRequest().getId()),
                String.valueOf(response.getBody().getRequestId()));
    }

    @Test
    public void get_issue_response_by_id_with_exception_test() {
        val id = 1;
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/")
                .concat(String.valueOf(1));

        try {
            restTemplate.getForEntity(url, IssueRequest.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(id)));
        }
    }

    @Test
    public void get_all_issue_responses_by_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/find_all")
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_responses_by_created_before_and_created_after_with_exception_test() {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/find_all")
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
    public void get_all_issue_responses_by_requester_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/find_all_by_requester")
                .concat("/").concat(String.valueOf(newIssueRequester.getId()))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_responses_by_requester_id_and_created_before_and_created_after_with_exception_test() {
        insertNewIssueRequester();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/find_all_by_requester")
                .concat("/").concat(String.valueOf(newIssueRequester.getId()))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
            assertThat(ex.getMessage(), containsString(String.valueOf(newIssueRequester.getId())));
        }
    }

    @Test
    public void get_all_issue_responses_by_request_id_and_created_before_and_created_after_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        insertNewIssueResponse2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/find_all_by_request")
                .concat("/").concat(String.valueOf(newIssueRequest.getId()))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        val response = restTemplate.getForEntity(url, PagedData.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        testPagedDataResponse(response.getBody());
    }

    @Test
    public void get_all_issue_responses_by_request_id_and_created_before_and_created_after_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/find_all_by_request")
                .concat("/").concat(String.valueOf(newIssueRequest.getId()))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        try {
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
            assertThat(ex.getMessage(), containsString(String.valueOf(newIssueRequest.getId())));
        }
    }

    @Test
    public void delete_issue_response_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/delete/")
                .concat(newIssueResponse1.getId().toString());

        val response = restTemplate.exchange(url,
                HttpMethod.DELETE, null, IssueResponse.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());
        assertEquals(String.valueOf(response.getBody().getId()), newIssueResponse1.getId().toString());

        try {
            issueResponseService.findById(response.getBody().getId());
        } catch (final ResponseStatusException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(response.getBody().getId())));
        }
    }

    @Test
    public void insert_issue_response_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setBody("Some response Body")
                .setRequesterId(newIssueRequester.getId())
                .setRequestId(newIssueRequest.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        val response = restTemplate.postForEntity(url,
                new HttpEntity<>(issueResponseToPost), IssueResponse.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        val found = issueResponseService.findById(response.getBody().getId());

        assertNotNull(found);

        assertEquals(String.valueOf(response.getBody().getId()), found.getId().toString());
        assertEquals(response.getBody().getBody(), found.getBody());
        assertEquals(String.valueOf(found.getRequester().getId()),
                String.valueOf(response.getBody().getRequesterId()));
        assertEquals(String.valueOf(found.getRequest().getId()),
                String.valueOf(response.getBody().getRequestId()));
    }

    @Test
    public void insert_issue_response_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setBody("      ")
                .setRequesterId(newIssueRequester.getId())
                .setRequestId(newIssueRequest.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueResponseToPost), IssueResponse.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString("must not be empty"));
        }
    }

    @Test
    public void update_issue_response_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setId(newIssueResponse1.getId())
                .setBody("Some new response Body")
                .setRequesterId(newIssueRequester.getId())
                .setRequestId(newIssueRequest.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        val response = restTemplate.postForEntity(url,
                new HttpEntity<>(issueResponseToPost), IssueResponse.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(response.getBody().getId()), newIssueResponse1.getId().toString());
        assertNotEquals(response.getBody().getBody(), newIssueResponse1.getBody());
    }

    @Test
    public void update_issue_response_with_exception_test() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setId(newIssueResponse1.getId())
                .setBody("    ")
                .setRequesterId(newIssueRequester.getId())
                .setRequestId(newIssueRequest.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueResponseToPost), IssueResponse.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString("must not be empty"));
        }
    }
}
