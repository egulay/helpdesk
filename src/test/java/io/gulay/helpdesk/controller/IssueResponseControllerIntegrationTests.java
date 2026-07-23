package io.gulay.helpdesk.controller;

import com.google.protobuf.util.JsonFormat;
import io.gulay.helpdesk.RestConfiguration;
import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.protoGen.*;
import io.gulay.helpdesk.data.model.IssueRequestModel;
import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.model.IssueResponseModel;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    public void get_issue_response_by_id_as_json_test() throws Exception {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val response = webClient.get()
                .uri("/api/v1/issue-responses/{id}", newIssueResponse1.getId())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(String.class)
                .block();

        assertNotNull(response);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());

        val builder = IssueResponse.newBuilder();
        JsonFormat.parser().merge(response.getBody(), builder);
        val body = builder.build();

        assertEquals(newIssueResponse1.getId().intValue(), body.getId());
        assertEquals(newIssueRequester.getId().intValue(), body.getRequesterId());
        assertEquals(newIssueRequest.getId().intValue(), body.getRequestId());
        assertEquals(newIssueResponse1.getBody(), body.getBody());
    }

    @Test
    public void get_issue_response_by_id_as_binary_protobuf_test() throws Exception {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();

        val protobufMediaType = MediaType.parseMediaType("application/x-protobuf");
        val response = webClient.get()
                .uri("/api/v1/issue-responses/{id}", newIssueResponse1.getId())
                .accept(protobufMediaType)
                .retrieve()
                .toEntity(byte[].class)
                .block();

        assertNotNull(response);
        assertTrue(protobufMediaType.isCompatibleWith(response.getHeaders().getContentType()));
        assertNotNull(response.getBody());

        val body = IssueResponse.parseFrom(response.getBody());
        assertEquals(newIssueResponse1.getId().intValue(), body.getId());
        assertEquals(newIssueRequester.getId().intValue(), body.getRequesterId());
        assertEquals(newIssueRequest.getId().intValue(), body.getRequestId());
        assertEquals(newIssueResponse1.getBody(), body.getBody());
    }

    @Test
    public void create_issue_response_from_json_and_return_json_test() throws Exception {
        insertNewIssueRequester();
        insertNewIssueRequest();
        val json = """
                {
                  "requestId": %d,
                  "requesterId": %d,
                  "body": "JSON issue response"
                }
                """.formatted(newIssueRequest.getId(), newIssueRequester.getId());

        val response = webClient.post()
                .uri("/api/v1/issue-responses")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .retrieve()
                .toEntity(String.class)
                .block();

        assertNotNull(response);
        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
        assertNotNull(response.getBody());

        val builder = IssueResponse.newBuilder();
        JsonFormat.parser().merge(response.getBody(), builder);
        val body = builder.build();

        assertTrue(body.getId() > 0);
        assertEquals(newIssueRequester.getId().intValue(), body.getRequesterId());
        assertEquals(newIssueRequest.getId().intValue(), body.getRequestId());
        assertEquals("JSON issue response", body.getBody());
    }

    @Test
    public void get_missing_issue_response_as_binary_protobuf_error_test() throws Exception {
        val missingId = Integer.MAX_VALUE;
        val protobufMediaType = MediaType.parseMediaType("application/x-protobuf");

        try {
            webClient.get()
                    .uri("/api/v1/issue-responses/{id}", missingId)
                    .accept(protobufMediaType)
                    .retrieve()
                    .toEntity(byte[].class)
                    .block();
            fail("Expected a not-found response");
        } catch (WebClientResponseException ex) {
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
            assertTrue(protobufMediaType.isCompatibleWith(ex.getHeaders().getContentType()));

            val error = ApiError.parseFrom(ex.getResponseBodyAsByteArray());
            assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatus());
            assertThat(error.getMessage()).contains(String.valueOf(missingId));
            assertEquals("/api/v1/issue-responses/" + missingId, error.getPath());
        }
    }

    @Test
    public void get_issue_response_by_id_with_exception_test() {
        val id = Integer.valueOf(RandomStringUtils.insecure().nextNumeric(5));
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
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
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
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
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
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
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
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
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
        val id = Integer.valueOf(RandomStringUtils.insecure().nextNumeric(5));

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
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getStatusCode().value());
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
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode().value());
            assertThat(body).contains("must not be blank");
        }
    }

    @Test
    public void insert_issue_response_with_exception_test_2() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        val id = Integer.valueOf(RandomStringUtils.insecure().nextNumeric(5));

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
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getStatusCode().value());
            assertThat(body).contains(String.valueOf(id));
        }
    }

    @Test
    public void insert_issue_response_with_exception_test_3() {
        insertNewIssueRequester();
        val id = Integer.valueOf(RandomStringUtils.insecure().nextNumeric(5));

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
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getStatusCode().value());
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
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getStatusCode().value());
            assertThat(body).contains("must not be blank");
        }
    }

    @Test
    public void update_issue_response_with_exception_test_2() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        val id = Integer.valueOf(RandomStringUtils.insecure().nextNumeric(5));

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
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getStatusCode().value());
            assertThat(body).contains(String.valueOf(id));
        }
    }

    @Test
    public void update_issue_response_with_exception_test_3() {
        insertNewIssueRequester();
        insertNewIssueRequest();
        insertNewIssueResponse1();
        val id = Integer.valueOf(RandomStringUtils.insecure().nextNumeric(5));

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
            assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), ex.getStatusCode().value());
            assertThat(body).contains(String.valueOf(id));
        }
    }
}
