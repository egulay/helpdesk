package com.helpdesk.data.util;

import jakarta.validation.ConstraintViolationException;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

public final class ExceptionMapperUtil {

    public static ResponseStatusException mapPersistenceException(Exception ex) {
        if (ex instanceof TransactionSystemException tse
                && tse.getRootCause() instanceof ConstraintViolationException cve) {
            return badRequestFrom(cve);
        }

        if (ex instanceof ConstraintViolationException cve) {
            return badRequestFrom(cve);
        }

        if (ex instanceof DataIntegrityViolationException dive) {
            return conflictFrom(dive);
        }

        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected persistence error");
    }

    private static ResponseStatusException badRequestFrom(ConstraintViolationException cve) {
        val detail = cve.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, detail);
    }

    private static ResponseStatusException conflictFrom(DataIntegrityViolationException ex) {
        ex.getMostSpecificCause();
        val msg = ex.getMostSpecificCause().getMessage();
        return new ResponseStatusException(HttpStatus.CONFLICT, msg);
    }
}