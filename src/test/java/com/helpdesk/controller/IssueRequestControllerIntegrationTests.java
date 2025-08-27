package com.helpdesk.controller;

import com.helpdesk.protoGen.*;
import com.helpdesk.RestConfiguration;
import com.helpdesk.TestBase;
import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class IssueRequestControllerIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester;

    private static IssueRequestModel newIssueRequest1;
    private static IssueRequestModel newIssueRequest2;

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
                .isActive(true)
                .fullName("test1_full_name")
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
                .isSolved(false)
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

        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueRequest.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueRequest1.getId()), String.valueOf(response.getBody().getId()));
        assertEquals(newIssueRequest1.getBody(), response.getBody().getBody());
        assertEquals(String.valueOf(newIssueRequest1.getRequester().getId()),
                String.valueOf(response.getBody().getRequesterId()));
    }

    @Test
    public void get_issue_request_by_id_with_exception_test() {
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/")
                .concat(String.valueOf(id));

        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("404");
            assertThat(ex.getMessage()).contains(String.valueOf(id));
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
    public void get_all_issue_requests_by_requester_id_and_created_before_and_created_after_with_exception_test() {
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("/").concat(String.valueOf(id))
                .concat("?createdBefore=").concat(String.valueOf(tomorrow))
                .concat("&createdAfter=").concat(String.valueOf(yesterday));

        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(yesterday));
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(tomorrow));
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(id));
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
    public void get_all_issue_requests_by_requester_id_with_exception_test() {
        insertNewIssueRequester();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all")
                .concat("/").concat(String.valueOf(newIssueRequester.getId()));

        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(newIssueRequester.getId()));
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

        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(PagedData.class)
                .block();

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
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(yesterday));
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(tomorrow));
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
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(yesterday));
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(tomorrow));
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(isSolved));
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
    public void get_all_issue_requests_by_solved_before_and_solved_after_with_exception_test() {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/find_all_solved")
                .concat("?solvedBefore=").concat(String.valueOf(tomorrow))
                .concat("&solvedAfter=").concat(String.valueOf(yesterday));

        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(yesterday));
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(tomorrow));
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

        val response = webClient.put()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueRequest.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueRequest1.getId()), String.valueOf(response.getBody().getId()));
        assertNotEquals(newIssueRequest1.getIsSolved(), response.getBody().getIsSolved().getData());
    }

    @Test
    public void solve_issue_request_with_exception_test() {
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));
        insertNewIssueRequester();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/solve/")
                .concat(String.valueOf(id));

        try {
            webClient.put()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("404");
            assertThat(ex.getMessage()).contains(String.valueOf(id));
        }
    }

    @Test
    public void delete_issue_request_test() {
        insertNewIssueRequester();
        insertNewIssueRequest1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/delete/")
                .concat(newIssueRequest1.getId().toString());

        val response = webClient.delete()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueRequest.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());
        assertEquals(String.valueOf(response.getBody().getId()), newIssueRequest1.getId().toString());

        try {
            issueRequesterService.findById(response.getBody().getId());
        } catch (final ResponseStatusException ex) {
            assertThat(ex.getMessage()).contains("404");
            assertThat(ex.getMessage()).contains(String.valueOf(response.getBody().getId()));
        }
    }

    @Test
    public void delete_issue_request_with_exception_test() {
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));
        insertNewIssueRequester();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/delete/")
                .concat(String.valueOf(id));

        try {
            webClient.delete()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getResponseBodyAsString()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(id));
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

        val response = webClient.post()
                .uri(toRelative(url))
                .bodyValue(issueRequestToPost)
                .retrieve()
                .toEntity(IssueRequest.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        val found = issueRequestService.findById(response.getBody().getId());

        assertNotNull(found);

        assertEquals(String.valueOf(response.getBody().getId()), found.getId().toString());
        assertEquals(response.getBody().getBody(), found.getBody());
    }

    @Test
    public void insert_issue_request_with_exception_test() {
        insertNewIssueRequester();

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setIsSolved(NullableBoolean.newBuilder().setData(false).build())
                .setBody("    ")
                .setRequesterId(newIssueRequester.getId())
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequestToPost)
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("400");
            assertThat(ex.getResponseBodyAsString()).contains("must not be blank");
        }
    }

    @Test
    public void insert_issue_request_with_exception_test_2() {
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setBody("hebe debe bidi bidi")
                .setRequesterId(id)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequestToPost)
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("406");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(id));
            assertThat(ex.getResponseBodyAsString()).contains("true");
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

        val response = webClient.post()
                .uri(toRelative(url))
                .bodyValue(issueRequestToPost)
                .retrieve()
                .toEntity(IssueRequest.class)
                .block();

        assertNotNull(response);
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
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequestToPost)
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("400");
            assertThat(ex.getResponseBodyAsString()).contains("must not be blank");
        }
    }

    @Test
    public void update_issue_request_with_exception_test_2() {
        insertNewIssueRequester();
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setId(id)
                .setRequesterId(newIssueRequester.getId())
                .setBody("new body hohoho")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequestToPost)
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("404");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(id));
        }
    }

    @Test
    public void update_issue_request_with_exception_test_3() {
        insertNewIssueRequester();
        insertNewIssueRequest1();
        val id = Integer.valueOf(RandomStringUtils.random(5, false, true));

        val issueRequestToPost = IssueRequest
                .newBuilder()
                .setId(newIssueRequest1.getId())
                .setRequesterId(id)
                .setBody("new body hehehe")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requests/save");

        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequestToPost)
                    .retrieve()
                    .toEntity(IssueRequest.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            assertThat(ex.getMessage()).contains("406");
            assertThat(ex.getResponseBodyAsString()).contains(String.valueOf(id));
            assertThat(ex.getResponseBodyAsString()).contains("true");
        }
    }
}
