package com.helpdesk.validation;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ValidationResult {
    private final boolean valid;
    private final String message;

    public static ValidationResult ok() {
        return new ValidationResult(true, null);
    }

    public static ValidationResult fail(String message) {
        return new ValidationResult(false, message);
    }

    private ValidationResult(boolean valid, String message) {
        this.valid = valid;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void throwIfInvalid() {
        if (!isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, getMessage());
        }
    }

    public void throwIfInvalid(String fieldName) {
        if (!isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " " + getMessage());
        }
    }

    public boolean writeIfInvalid(String fieldName, HttpServletResponse response) throws IOException {
        if (!isValid()) {
            response.getWriter().write(HttpStatus.BAD_REQUEST.value()
                    + fieldName + " " + getMessage());
            return false;
        }

        return true;
    }

    public String getMessage() {
        return message;
    }
}
