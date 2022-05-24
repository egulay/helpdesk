package com.helpdesk.controller;

import com.helpdesk.protoGen.*;
import com.helpdesk.RestConfiguration;
import com.helpdesk.TestBase;
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class IssueRequesterControllerIntegrationTests extends TestBase {
    private static IssueRequesterModel newIssueRequester1;

    private static IssueRequesterModel newIssueRequester2;

    public void insertNewIssueRequester1() {
        newIssueRequester1 = issueRequesterService.save(IssueRequesterModel
                .builder()
                .fullName("test1 full name")
                .email("test1c@email.com")
                .build());
    }

    public void insertNewIssueRequester2() {
        newIssueRequester2 = issueRequesterService.save(IssueRequesterModel
                .builder()
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

        val response = restTemplate.getForEntity(url, IssueRequester.class);

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
            restTemplate.getForEntity(url, IssueRequester.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(id)));
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

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
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

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
        }
    }

    @Test
    public void get_issue_requesters_test() {
        insertNewIssueRequester1();
        insertNewIssueRequester2();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/find_all");

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
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

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
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

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString("oh_noes!"));
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

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(yesterday)));
            assertThat(ex.getMessage(), containsString(String.valueOf(tomorrow)));
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

        val response = restTemplate.getForEntity(url, PagedData.class);

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
            restTemplate.getForEntity(url, PagedData.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString("@oh_noes.com"));
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

        val response = restTemplate.exchange(url,
                HttpMethod.PUT, null, IssueRequester.class);

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

        val response = restTemplate.exchange(url,
                HttpMethod.DELETE, null, IssueRequester.class);

        assertTrue(StringUtils.isNotBlank(response.toString()));
        assertNotNull(response.getBody());
        assertEquals(String.valueOf(response.getBody().getId()), (newIssueRequester1.getId().toString()));

        try {
            issueRequesterService.findById(response.getBody().getId());
        } catch (final ResponseStatusException ex) {
            assertThat(ex.getMessage(), containsString("404"));
            assertThat(ex.getMessage(), containsString(String.valueOf(response.getBody().getId())));
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

        val response = restTemplate.postForEntity(url,
                new HttpEntity<>(issueRequesterToPost), IssueRequester.class);

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
                .setFullName("   ")
                .setEmail("hede@hodo.com")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueRequesterToPost), IssueRequester.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString("must not be empty"));
        }
    }

    @Test
    public void insert_issue_requester_with_exception_test_2() {
        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setFullName("   ")
                .setEmail("hede@hodo.com")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueRequesterToPost), IssueRequester.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString("must not be empty"));
        }
    }

    @Test
    public void insert_issue_requester_with_exception_test_3() {
        val email = "hede@hodo";
        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setFullName("Abuzer Kadayif")
                .setEmail(email)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");

        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueRequesterToPost), IssueRequester.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString(email));
        }
    }

    @Test
    public void update_issue_requester_test() {
        insertNewIssueRequester1();

        val issueRequesterToPost = IssueRequester
                .newBuilder()
                .setId(newIssueRequester1.getId())
                .setFullName("Hebee Bidi")
                .setEmail("hebee@hodo.net")
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");

        val response = restTemplate.postForEntity(url,
                new HttpEntity<>(issueRequesterToPost), IssueRequester.class);

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
                .setFullName("Hede Hodo")
                .setEmail(email)
                .build();

        val url = RestConfiguration.LOCALHOST
                .concat(String.valueOf(port))
                .concat("/v1/issue_requesters/save");
        try {
            restTemplate.postForEntity(url,
                    new HttpEntity<>(issueRequesterToPost), IssueRequester.class);
        } catch (final HttpClientErrorException ex) {
            assertThat(ex.getMessage(), containsString("400"));
            assertThat(ex.getMessage(), containsString(email));
        }
    }
}
