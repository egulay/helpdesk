package com.helpdesk.mcp.tooling.assistant;

import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.model.IssueResponseModel;
import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.service.IssueResponseService;
import com.helpdesk.data.util.SortDirection;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.List;

@Service
public class HelpdeskContextBuilder {

    private final IssueRequestService issueRequestService;
    private final IssueRequesterService issueRequesterService;
    private final IssueResponseService issueResponseService;

    public HelpdeskContextBuilder(
            IssueRequestService issueRequestService,
            IssueRequesterService issueRequesterService,
            IssueResponseService issueResponseService
    ) {
        this.issueRequestService = issueRequestService;
        this.issueRequesterService = issueRequesterService;
        this.issueResponseService = issueResponseService;
    }

    public HelpdeskTicketContext buildTicketContext(Integer requestId) {
        try {
            val request = issueRequestService.findById(requestId);

            val requesterId = request.getRequester() == null
                    ? null
                    : request.getRequester().getId();

            if (requesterId == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Requester id is missing for requestId: " + requestId
                );
            }

            val requester = findRequesterSafely(requesterId);
            val responses = findResponsesSafely(requestId);

            return new HelpdeskTicketContext(
                    toTicketRequest(request, requesterId),
                    toTicketRequester(requester),
                    responses.stream()
                            .map(this::toTicketResponse)
                            .toList()
            );

        } catch (ResponseStatusException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not build helpdesk ticket context for requestId: " + requestId,
                    ex
            );
        }
    }

    private IssueRequesterModel findRequesterSafely(Integer requesterId) {
        try {
            return issueRequesterService.findById(requesterId);

        } catch (ResponseStatusException ex) {
            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Could not find requester for requesterId: " + requesterId,
                    ex
            );
        }
    }

    private Collection<IssueResponseModel> findResponsesSafely(Integer requestId) {
        try {
            return issueResponseService
                    .findAllByRequestId(requestId, 0, 100, "created", SortDirection.Ascending)
                    .getContent();

        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return List.of();
            }

            throw new ResponseStatusException(
                    ex.getStatusCode(),
                    "Could not find responses for requestId: " + requestId,
                    ex
            );
        }
    }

    private HelpdeskTicketContext.TicketRequest toTicketRequest(
            IssueRequestModel request,
            Integer requesterId
    ) {
        return new HelpdeskTicketContext.TicketRequest(
                request.getId(),
                requesterId,
                request.getBody(),
                request.getIsSolved(),
                request.getCreated() == null ? null : request.getCreated().toInstant().toString(),
                request.getSolved() == null ? null : request.getSolved().toInstant().toString()
        );
    }

    private HelpdeskTicketContext.TicketRequester toTicketRequester(IssueRequesterModel requester) {
        return new HelpdeskTicketContext.TicketRequester(
                requester.getId(),
                requester.getFullName(),
                requester.getEmail(),
                requester.getIsActive(),
                requester.getCreated() == null ? null : requester.getCreated().toInstant().toString()
        );
    }

    private HelpdeskTicketContext.TicketResponse toTicketResponse(IssueResponseModel response) {
        val requestId = response.getRequest() == null
                ? null
                : response.getRequest().getId();

        val requesterId = response.getRequester() == null
                ? null
                : response.getRequester().getId();

        return new HelpdeskTicketContext.TicketResponse(
                response.getId(),
                requestId,
                requesterId,
                response.getBody(),
                response.getCreated() == null ? null : response.getCreated().toInstant().toString()
        );
    }
}