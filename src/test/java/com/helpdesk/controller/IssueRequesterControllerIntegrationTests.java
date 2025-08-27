package com.helpdesk.controller;

import com.helpdesk.protoGen.*;
import com.helpdesk.RestConfiguration;
import com.helpdesk.TestBase;
import com.helpdesk.data.model.IssueRequesterModel;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class IssueRequesterControllerIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester1;

    private static IssueRequesterModel newIssueRequester2;

    @Before
    public void initWebClientBaseUrl() {
        this.webClient = this.webClientBuilder
                .baseUrl(RestConfiguration.LOCALHOST + port)
                .build();
    }

    private String toRelative(String url) {
        return url.replace(RestConfiguration.LOCALHOST.concat(String.valueOf(port)), "");
    }

    public void insertNewIssueRequester1() {
        newIssueRequester1 = issueRequesterService.save(IssueRequesterModel
                .builder()
                .isActive(true)
                .fullName("test1 full name")
                .email("test1c@email.com")
                .build());
    }

    public void insertNewIssueRequester2() {
        newIssueRequester2 = issueRequesterService.save(IssueRequesterModel
                .builder()
                .isActive(true)
                .fullName("test2_full_name")
                .email("test2c@email.com")
                .build());
    }

    public void testPagedDataResponse(PagedData model) {
        assertFalse(model.getIssueRequesters().getIssueRequestersList().isEmpty());
        assertEquals(2, model.getTotalElements());
        assertEquals(1, model.getTotalPages());
        assertEquals(2, model.getNumberOfElements());

        assertTrue(model.getIssueRequesters().getIssueRequestersList()
                .stream()
                .anyMatch(f -> String.valueOf(f.getId()).equals(newIssueRequester1.getId().toString())));

        assertTrue(model.getIssueRequesters().getIssueRequestersList()
                .stream()
                .anyMatch(f -> String.valueOf(f.getId()).equals(newIssueRequester2.getId().toString())));
    }

    @Before
    public void setup() {
        issueRequesterService.hardDeleteAll();
    }

    @Test
    public void get_issue_requester_by_id_test() {
        insertNewIssueRequester1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/")
                .concat(newIssueRequester1.getId().toString());
        val response = webClient.get()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueRequester.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueRequester1.getId()), String.valueOf(response.getBody().getId()));
        assertEquals(newIssueRequester1.getFullName(), response.getBody().getFullName());
        assertEquals(newIssueRequester1.getEmail(), response.getBody().getEmail());
    }

    @Test
    public void get_issue_requester_by_id_with_exception_test() {
        val id = 1;
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/")
                .concat(String.valueOf(id));
        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(IssueRequester.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains(String.valueOf(id));
        }
    }

    @Test
    public void get_issue_requesters_by_created_before_and_created_after_and_is_active_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all")
                .concat("?isActive=true")
                .concat("&createdBefore=").concat(String.valueOf(tomorrow))
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
    public void get_issue_requesters_by_created_before_and_created_after_and_is_active_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all")
                .concat("?isActive=false")
                .concat("&createdBefore=").concat(String.valueOf(tomorrow))
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
    public void get_issue_requesters_by_created_before_and_created_after_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all")
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
    public void get_issue_requesters_by_created_before_and_created_after_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all")
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
    public void get_issue_requesters_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all");
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
    public void get_issue_requesters_with_exception_test() {
        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all");
        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
        }
    }

    @Test
    public void get_issue_requesters_by_created_before_and_created_after_and_full_name_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_full_name")
                .concat("/name")
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
    public void get_issue_requesters_by_created_before_and_created_after_and_full_name_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_full_name")
                .concat("/oh_noes!")
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
            assertThat(body).contains("oh_noes!");
            assertThat(body).contains(String.valueOf(yesterday));
            assertThat(body).contains(String.valueOf(tomorrow));
        }
    }

    @Test
    public void get_issue_requesters_by_full_name_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_full_name")
                .concat("/name");
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
    public void get_issue_requesters_by_full_name_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_full_name")
                .concat("/oh_noes!");
        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains("oh_noes!");
        }
    }

    @Test
    public void get_issue_requesters_by_created_before_and_created_after_and_email_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_email")
                .concat("/@email.com")
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
    public void get_issue_requesters_by_created_before_and_created_after_and_email_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        val tomorrow = Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_email")
                .concat("/oh_noes.com")
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
            assertThat(body).contains("oh_noes.com");
            assertThat(body).contains(String.valueOf(yesterday));
            assertThat(body).contains(String.valueOf(tomorrow));
        }
    }

    @Test
    public void get_issue_requesters_by_email_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_email")
                .concat("/@email.com");
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
    public void get_issue_requesters_by_email_with_exception_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all_by_email")
                .concat("/@oh_noes.com");
        try {
            webClient.get()
                    .uri(toRelative(url))
                    .retrieve()
                    .toEntity(PagedData.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.NOT_FOUND.value(), ex.getRawStatusCode());
            assertThat(body).contains("@oh_noes.com");
        }
    }

    @Test
    public void toggle_issue_requester_activation_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/toggle_activation/")
                .concat(String.valueOf(newIssueRequester1.getId()));
        val response = webClient.put()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueRequester.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(newIssueRequester1.getId()), String.valueOf(response.getBody().getId()));
        assertNotEquals(newIssueRequester1.getIsActive(), response.getBody().getIsActive().getData());
    }

    @Test
    public void delete_issue_requester_test() {
        insertNewIssueRequester1();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/delete/")
                .concat(newIssueRequester1.getId().toString());
        val response = webClient.delete()
                .uri(toRelative(url))
                .retrieve()
                .toEntity(IssueRequester.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());
        assertEquals(String.valueOf(response.getBody().getId()), (newIssueRequester1.getId().toString()));

        try {
            issueRequesterService.findById(response.getBody().getId());
        } catch (final ResponseStatusException ex) {
            assertThat(ex.getMessage()).contains("404");
            assertThat(ex.getMessage()).contains(String.valueOf(response.getBody().getId()));
        }
    }

    @Test
    public void insert_issue_requester_test() {
        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setFullName("Hede Hodo")
                .setEmail("hede@hodo.com")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");
        val response = webClient.post()
                .uri(toRelative(url))
                .bodyValue(issueRequesterToPost)
                .retrieve()
                .toEntity(IssueRequester.class)
                .block();

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        val found = issueRequesterService.findById(response.getBody().getId());

        assertNotNull(found);

        assertEquals(String.valueOf(response.getBody().getId()), found.getId().toString());
        assertEquals(response.getBody().getFullName(), found.getFullName());
        assertEquals(response.getBody().getEmail(), found.getEmail());
    }

    @Test
    public void insert_issue_requester_with_exception_test() {
        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setIsActive(NullableBoolean.newBuilder().setData(true).build())
                .setFullName("   ")
                .setEmail("hede@hodo.com")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");
        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequesterToPost)
                    .retrieve()
                    .toEntity(IssueRequester.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getRawStatusCode());
            assertThat(body).contains("must not be blank");
        }
    }

    @Test
    public void insert_issue_requester_with_exception_test_3() {
        val email = "hede@hodo";
        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setIsActive(NullableBoolean.newBuilder().setData(true).build())
                .setFullName("Abuzer Kadayif")
                .setEmail(email)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");
        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequesterToPost)
                    .retrieve()
                    .toEntity(IssueRequester.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getRawStatusCode());
            assertThat(body).contains(email);
        }
    }

    @Test
    public void update_issue_requester_test() {
        insertNewIssueRequester1();

        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setId(newIssueRequester1.getId())
                .setIsActive(NullableBoolean.newBuilder().setData(true).build())
                .setFullName("Hebee Bidi")
                .setEmail("hebee@hodo.net")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");
        val response = webClient.post()
                .uri(toRelative(url))
                .bodyValue(issueRequesterToPost)
                .retrieve()
                .toEntity(IssueRequester.class)
                .block();

        assertNotNull(response);
        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());

        assertEquals(String.valueOf(response.getBody().getId()), newIssueRequester1.getId().toString());
        assertNotEquals(response.getBody().getFullName(), newIssueRequester1.getFullName());
        assertNotEquals(response.getBody().getEmail(), newIssueRequester1.getEmail());
    }

    @Test
    public void update_issue_requester_with_exception_test() {
        insertNewIssueRequester1();
        val email = "hede@hodo";
        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setId(newIssueRequester1.getId())
                .setIsActive(NullableBoolean.newBuilder().setData(true).build())
                .setFullName("Hede Hodo")
                .setEmail(email)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");
        try {
            webClient.post()
                    .uri(toRelative(url))
                    .bodyValue(issueRequesterToPost)
                    .retrieve()
                    .toEntity(IssueRequester.class)
                    .block();
        } catch (final WebClientResponseException ex) {
            final String body = ex.getResponseBodyAsString();
            assertEquals(HttpStatus.BAD_REQUEST.value(), ex.getRawStatusCode());
            assertThat(body).contains(email);
        }
    }
}
