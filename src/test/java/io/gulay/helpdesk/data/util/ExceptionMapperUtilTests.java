package io.gulay.helpdesk.data.util;

import io.gulay.helpdesk.data.model.IssueRequesterModel;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionMapperUtilTests {

    private static Validator validator;

    @BeforeAll
    public static void initValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static ConstraintViolationException invalidRequesterViolations() {
        val violations = validator.validate(IssueRequesterModel
                .builder()
                .fullName("")
                .email("not-an-email")
                .isActive(true)
                .build());

        assertFalse(violations.isEmpty(), "expected the invalid model to produce violations");

        return new ConstraintViolationException(violations);
    }

    @Test
    public void maps_constraint_violation_to_bad_request_test() {
        val mapped = ExceptionMapperUtil.mapPersistenceException(invalidRequesterViolations());

        assertEquals(HttpStatus.BAD_REQUEST, mapped.getStatusCode());
        assertNotNull(mapped.getReason());
        assertTrue(mapped.getReason().contains("fullName"));
        assertTrue(mapped.getReason().contains("email"));
    }

    @Test
    public void joins_violation_details_in_sorted_order_test() {
        val reason = ExceptionMapperUtil.mapPersistenceException(invalidRequesterViolations()).getReason();

        assertNotNull(reason);
        val parts = reason.split("; ");
        assertTrue(parts.length >= 2);

        val sorted = parts.clone();
        java.util.Arrays.sort(sorted);
        assertArrayEquals(sorted, parts);
    }

    @Test
    public void unwraps_transaction_system_exception_root_cause_test() {
        val wrapped = new TransactionSystemException(
                "Could not commit JPA transaction",
                invalidRequesterViolations()
        );

        val mapped = ExceptionMapperUtil.mapPersistenceException(wrapped);

        assertEquals(HttpStatus.BAD_REQUEST, mapped.getStatusCode());
        assertNotNull(mapped.getReason());
        assertTrue(mapped.getReason().contains("email"));
    }

    @Test
    public void maps_data_integrity_violation_to_conflict_test() {
        val mapped = ExceptionMapperUtil.mapPersistenceException(
                new DataIntegrityViolationException("duplicate key value violates unique constraint")
        );

        assertEquals(HttpStatus.CONFLICT, mapped.getStatusCode());
        assertEquals("duplicate key value violates unique constraint", mapped.getReason());
    }

    @Test
    public void maps_unknown_exception_to_internal_server_error_test() {
        val mapped = ExceptionMapperUtil.mapPersistenceException(new IllegalStateException("boom"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, mapped.getStatusCode());
        assertEquals("Unexpected persistence error", mapped.getReason());
    }

    @Test
    public void maps_transaction_system_exception_without_violations_to_internal_server_error_test() {
        val mapped = ExceptionMapperUtil.mapPersistenceException(
                new TransactionSystemException("no root cause")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, mapped.getStatusCode());
    }
}
