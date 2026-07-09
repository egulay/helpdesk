package com.helpdesk.mcp.assistant;

import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.model.IssueResponseModel;
import lombok.val;

import java.util.Collection;
import java.util.stream.Collectors;

public record HelpdeskTicketContext(
        IssueRequestModel request,
        IssueRequesterModel requester,
        Collection<IssueResponseModel> responses
) {

    public String toPromptText() {
        val responsesText = responses.stream()
                .map(response -> "- " + response)
                .collect(Collectors.joining(System.lineSeparator()));

        return """
                Requester:
                %s

                Request:
                %s

                Responses:
                %s
                """.formatted(
                requester,
                request,
                responsesText
        );
    }
}