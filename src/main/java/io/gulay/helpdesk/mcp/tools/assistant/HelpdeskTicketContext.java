package io.gulay.helpdesk.mcp.tools.assistant;

import lombok.val;

import java.util.Collection;
import java.util.stream.Collectors;

public record HelpdeskTicketContext(
        TicketRequest request,
        TicketRequester requester,
        Collection<TicketResponse> responses
) {

    public String toPromptText() {
        val responsesText = responses == null || responses.isEmpty()
                ? "No responses found."
                : responses.stream()
                .map(response -> "- " + response.toPromptText())
                .collect(Collectors.joining(System.lineSeparator()));

        return """
                Requester:
                %s
                
                Request:
                %s
                
                Responses:
                %s
                """.formatted(
                requester.toPromptText(),
                request.toPromptText(),
                responsesText
        );
    }

    public record TicketRequester(
            Integer id,
            String fullName,
            String email,
            Boolean isActive,
            String created
    ) {
        public String toPromptText() {
            return """
                    id: %s
                    fullName: %s
                    email: %s
                    isActive: %s
                    created: %s
                    """.formatted(id, fullName, email, isActive, created);
        }
    }

    public record TicketRequest(
            Integer id,
            Integer requesterId,
            String requestBody,
            Boolean isSolved,
            String created,
            String solved
    ) {
        public String toPromptText() {
            return """
                    id: %s
                    requesterId: %s
                    requestBody: %s
                    isSolved: %s
                    created: %s
                    solved: %s
                    """.formatted(id, requesterId, requestBody, isSolved, created, solved);
        }
    }

    public record TicketResponse(
            Integer id,
            Integer requestId,
            Integer requesterId,
            String responseBody,
            String created
    ) {
        public String toPromptText() {
            return """
                    id: %s
                    requestId: %s
                    requesterId: %s
                    responseBody: %s
                    created: %s
                    """.formatted(id, requestId, requesterId, responseBody, created);
        }
    }
}
