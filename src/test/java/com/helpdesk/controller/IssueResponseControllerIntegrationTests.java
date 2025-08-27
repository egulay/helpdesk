package com.helpdesk.controller;

import com.helpdesk.RestConfiguration;
import com.helpdesk.TestBase;
import com.helpdesk.protoGen.*;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.model.IssueResponseModel;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class IssueResponseControllerIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester;

    private static IssueRequestModel newIssueRequest;

    private static IssueResponseModel newIssueResponse1;
    private static IssueResponseModel newIssueResponse2;

    @Before
    public void initWebClientBaseUrl() {
        this.webClient = this.webClientBuilder
                .baseUrl(RestConfiguration.LOCALHOST + port)
                .build();
    }

    private String toRelative(String url) {
        return url.replace(RestConfiguration.LOCALHOST.concat(String.valueOf(port)), "");
    }

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

        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueResponse.class)
                .block();

        assertNotNull(response);
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
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/")
                .concat(String.valueOf(id));

        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
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

        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(PagedData.class)
                .block();

        assertNotNull(response);
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
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(yesterday));
            assertThat(body).contains(String.valueOf(tomorrow));
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

        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(PagedData.class)
                .block();

        assertNotNull(response);
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
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(yesterday));
            assertThat(body).contains(String.valueOf(tomorrow));
            assertThat(body).contains(String.valueOf(newIssueRequester.getId()));
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

        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(PagedData.class)
                .block();

        assertNotNull(response);
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
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(yesterday));
            assertThat(body).contains(String.valueOf(tomorrow));
            assertThat(body).contains(String.valueOf(newIssueRequest.getId()));
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

        val response = webClient.delete()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueResponse.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());
        assertEquals(String.valueOf(response.getBody().getId()), newIssueResponse1.getId().toString());

        try {
            issueResponseService.findById(response.getBody().getId());
        } catch (final ResponseStatusException ex) {
            assertThat(ex.getMessage()).contains("404");
            assertThat(ex.getMessage()).contains(String.valueOf(response.getBody().getId()));
        }
    }

    @Test
    public void delete_issue_response_with_exception_test() {
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/delete/")
                .concat(String.valueOf(id));

        try {
            webClient.delete()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
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

        val response = webClient.post()
                .uri(toRelative(url))
                .bodyValue(issueResponseToPost)
                .retrieve()
                .toEntity(IssueResponse.class)
                .block();

        assertNotNull(response);
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
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueResponseToPost)
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getRawStatusCode());
            assertThat(body).contains("must not be blank");
        }
    }

    @Test
    public void insert_issue_response_with_exception_test_2() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setBody("some body")
                .setRequesterId(id)
                .setRequestId(newIssueRequest.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueResponseToPost)
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
        }
    }

    @Test
    public void insert_issue_response_with_exception_test_3() {
        insertNewIssueRequester();
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setBody("some body")
                .setRequesterId(newIssueRequester.getId())
                .setRequestId(id)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueResponseToPost)
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
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

        val response = webClient.post()
                .uri(toRelative(url))
                .bodyValue(issueResponseToPost)
                .retrieve()
                .toEntity(IssueResponse.class)
                .block();

        assertNotNull(response);
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
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueResponseToPost)
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getRawStatusCode());
            assertThat(body).contains("must not be blank");
        }
    }

    @Test
    public void update_issue_response_with_exception_test_2() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setId(newIssueResponse1.getId())
                .setBody("some body")
                .setRequesterId(id)
                .setRequestId(newIssueRequest.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueResponseToPost)
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
        }
    }

    @Test
    public void update_issue_response_with_exception_test_3() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueResponseToPost = IssueResponse
                .newBuilder()
                .setId(newIssueResponse1.getId())
                .setBody("some body")
                .setRequesterId(id)
                .setRequestId(id)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_responses/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueResponseToPost)
                    .retrieve()
                    .toEntity(IssueResponse.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
        }
    }
}
