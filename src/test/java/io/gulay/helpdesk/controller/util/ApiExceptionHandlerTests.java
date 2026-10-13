package io.gulay.helpdesk.controller.util;

import jakarta.validation.ConstraintViolationException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ApiExceptionHandlerTests {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    private static MockHttpServletRequest request(String accept) {
        val request = new MockHttpServletRequest("GET", "/api/v1/issue-requesters/1");
        if (accept != null) {
            request.addHeader("Accept", accept);
        }
        return request;
    }

    @Test
    public void maps_response_status_exception_to_matching_api_error_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:1"),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("Not Found", response.getBody().getError());
        assertEquals("requesterId:1", response.getBody().getMessage());
        assertEquals("/api/v1/issue-requesters/1", response.getBody().getPath());
        assertTrue(response.getBody().getTimestamp() > 0);
    }

    @Test
    public void falls_back_to_reason_phrase_when_no_reason_is_given_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertNotNull(response.getBody());
        assertEquals("Not Acceptable", response.getBody().getMessage());
    }

    @Test
    public void falls_back_to_internal_server_error_for_unresolvable_status_codes_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatusCode.valueOf(599), "odd status"),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    public void maps_illegal_argument_exception_to_bad_request_test() {
        val response = handler.handleBadRequest(
                new IllegalArgumentException("sortBy must not be blank"),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("sortBy must not be blank", response.getBody().getMessage());
    }

    @Test
    public void maps_constraint_violation_exception_to_bad_request_test() {
        val response = handler.handleBadRequest(
                new ConstraintViolationException("email: must be a valid address", Set.of()),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void maps_missing_resource_to_not_found_test() {
        val response = handler.handleNotFound(
                new NoResourceFoundException(HttpMethod.GET, "/missing", "/missing"),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    public void hides_unexpected_exception_details_test() {
        val response = handler.handleUnexpected(
                new RuntimeException("connection string leaked here"),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Unexpected server error", response.getBody().getMessage());
        assertFalse(response.getBody().getMessage().contains("connection string"));
    }

    @Test
    public void returns_binary_error_for_protobuf_only_clients_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:1"),
                request("application/x-protobuf")
        );

        assertEquals(MediaType.parseMediaType("application/x-protobuf"), response.getHeaders().getContentType());
    }

    @Test
    public void returns_json_error_for_json_clients_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:1"),
                request(MediaType.APPLICATION_JSON_VALUE)
        );

        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void returns_json_error_when_no_accept_header_is_sent_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:1"),
                request(null)
        );

        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }

    @Test
    public void wildcard_accept_header_selects_protobuf_error_body_test() {
        // Documents current behavior: */* is compatible with application/x-protobuf, so a
        // client that states no preference receives binary ApiError bytes, not Protobuf JSON.
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:1"),
                request(MediaType.ALL_VALUE)
        );

        assertEquals(
                MediaType.parseMediaType("application/x-protobuf"),
                response.getHeaders().getContentType()
        );
    }

    @Test
    public void returns_json_error_for_mixed_accept_headers_test() {
        val response = handler.handleResponseStatus(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "requesterId:1"),
                request("application/x-protobuf, application/json")
        );

        assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
    }
}
