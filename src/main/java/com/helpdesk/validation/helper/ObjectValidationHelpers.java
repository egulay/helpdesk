package com.helpdesk.validation.helper;

import com.helpdesk.validation.SimpleValidation;
import com.helpdesk.validation.Validation;

import java.util.Objects;

public class ObjectValidationHelpers {
    public static Validation<Object> notNull = SimpleValidation.from(Objects::nonNull, "must not be null.");
}
