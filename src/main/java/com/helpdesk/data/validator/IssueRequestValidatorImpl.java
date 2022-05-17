package com.helpdesk.data.validator;

import com.helpdesk.data.model.IssueRequestModel;
import org.springframework.stereotype.Component;

import static com.helpdesk.validation.Constants.REQUESTER_ID_FIELD_FOR_VALIDATION;
import static com.helpdesk.validation.Constants.REQUEST_BODY_FIELD_FOR_VALIDATION;
import static com.helpdesk.validation.helper.StringValidationHelpers.notBlank;
import static com.helpdesk.validation.helper.ObjectValidationHelpers.notNull;

@Component
public class IssueRequestValidatorImpl implements IssueRequestValidator {
    @Override
    public void validate(IssueRequestModel model) {
        notBlank.test(model.getBody()).throwIfInvalid(REQUEST_BODY_FIELD_FOR_VALIDATION);
        notBlank.test(model.getRequester().getId().toString()).throwIfInvalid(REQUESTER_ID_FIELD_FOR_VALIDATION);
        notNull.test(model.getRequester().getId().toString()).throwIfInvalid(REQUESTER_ID_FIELD_FOR_VALIDATION);
    }
}
