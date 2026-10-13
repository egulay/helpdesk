package io.gulay.helpdesk.data.model;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IssueModelEqualityTests {

    @Test
    public void requesters_with_same_id_are_equal_test() {
        val left = IssueRequesterModel.builder().id(1).fullName("left").build();
        val right = IssueRequesterModel.builder().id(1).fullName("right").build();

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }

    @Test
    public void requesters_with_different_ids_are_not_equal_test() {
        assertNotEquals(
                IssueRequesterModel.builder().id(1).build(),
                IssueRequesterModel.builder().id(2).build()
        );
    }

    @Test
    public void unsaved_requesters_are_never_equal_test() {
        // A null id means the entity is not persisted yet, so identity cannot be established.
        assertNotEquals(
                IssueRequesterModel.builder().fullName("same").build(),
                IssueRequesterModel.builder().fullName("same").build()
        );
    }

    @Test
    public void requester_is_not_equal_to_null_or_other_types_test() {
        val requester = IssueRequesterModel.builder().id(1).build();

        assertNotEquals(null, requester);
        assertNotEquals("1", requester);
        assertEquals(requester, requester);
    }

    @Test
    public void requests_with_same_id_are_equal_test() {
        val left = IssueRequestModel.builder().id(7).body("left").build();
        val right = IssueRequestModel.builder().id(7).body("right").build();

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }

    @Test
    public void requests_with_different_ids_are_not_equal_test() {
        assertNotEquals(
                IssueRequestModel.builder().id(7).build(),
                IssueRequestModel.builder().id(8).build()
        );
    }

    @Test
    public void responses_with_same_id_are_equal_test() {
        val left = IssueResponseModel.builder().id(3).body("left").build();
        val right = IssueResponseModel.builder().id(3).body("right").build();

        assertEquals(left, right);
        assertEquals(left.hashCode(), right.hashCode());
    }

    @Test
    public void responses_with_different_ids_are_not_equal_test() {
        assertNotEquals(
                IssueResponseModel.builder().id(3).build(),
                IssueResponseModel.builder().id(4).build()
        );
    }

    @Test
    public void unsaved_requests_are_never_equal_test() {
        assertNotEquals(
                IssueRequestModel.builder().body("same").build(),
                IssueRequestModel.builder().body("same").build()
        );
    }

    @Test
    public void unsaved_responses_are_never_equal_test() {
        assertNotEquals(
                IssueResponseModel.builder().body("same").build(),
                IssueResponseModel.builder().body("same").build()
        );
    }

    @Test
    public void request_is_not_equal_to_null_or_other_types_test() {
        val request = IssueRequestModel.builder().id(7).build();

        assertNotEquals(null, request);
        assertNotEquals("7", request);
        assertEquals(request, request);
    }

    @Test
    public void response_is_not_equal_to_null_or_other_types_test() {
        val response = IssueResponseModel.builder().id(3).build();

        assertNotEquals(null, response);
        assertNotEquals("3", response);
        assertEquals(response, response);
    }

    @Test
    public void models_of_different_entity_types_are_not_equal_test() {
        assertNotEquals(
                IssueRequesterModel.builder().id(1).build(),
                IssueRequestModel.builder().id(1).build()
        );
    }
}
