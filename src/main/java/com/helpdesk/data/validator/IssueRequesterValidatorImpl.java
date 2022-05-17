package com.helpdesk.data.validator;

import com.helpdesk.data.model.IssueRequesterModel;
import org.springframework.stereotype.Component;

import static com.helpdesk.validation.Constants.REQUESTER_EMAIL_FIELD_FOR_VALIDATION;
import static com.helpdesk.validation.Constants.REQUESTER_FULL_NAME_FIELD_FOR_VALIDATION;
import static com.helpdesk.validation.helper.StringValidationHelpers.notBlank;
import static com.helpdesk.validation.helper.StringValidationHelpers.notValidEmail;

@Component
public class IssueRequesterValidatorImpl implements IssueRequesterValidator {
    @Override
    public void validate(IssueRequesterModel model) {
        notBlank.test(model.getFullName()).throwIfInvalid(REQUESTER_FULL_NAME_FIELD_FOR_VALIDATION);
        notBlank.test(model.getEmail()).throwIfInvalid(REQUESTER_EMAIL_FIELD_FOR_VALIDATION);
        notValidEmail.test(model.getEmail()).throwIfInvalid(REQUESTER_EMAIL_FIELD_FOR_VALIDATION);
    }
}
