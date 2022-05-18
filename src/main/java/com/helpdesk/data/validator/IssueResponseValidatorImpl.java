package com.helpdesk.data.validator;

import com.helpdesk.data.model.IssueResponseModel;
import org.springframework.stereotype.Component;

import static com.helpdesk.validation.Constants.*;
import static com.helpdesk.validation.helper.ObjectValidationHelpers.notNull;
import static com.helpdesk.validation.helper.StringValidationHelpers.notBlank;

@Component
public class IssueResponseValidatorImpl implements IssueResponseValidator {
    @Override
    public void validate(IssueResponseModel model) {
        notNull.test(model.getRequest().getId()).throwIfInvalid(REQUEST_ID_FIELD_FOR_VALIDATION);
        notNull.test(model.getRequester().getId()).throwIfInvalid(REQUESTER_ID_FIELD_FOR_VALIDATION);
        notBlank.test(model.getBody()).throwIfInvalid(RESPONSE_BODY_FIELD_FOR_VALIDATION);
    }
}
