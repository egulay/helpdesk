package com.helpdesk.mcp.assistant;

import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.service.IssueResponseService;
import com.helpdesk.data.util.SortDirection;
import lombok.val;
import org.springframework.stereotype.Service;


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
        val request = issueRequestService.findById(requestId);

        val requester = issueRequesterService.findById(request.getRequester().getId());

        val responses = issueResponseService
                        .findAllByRequestId(requestId, 0, 100, "created", SortDirection.Ascending)
                        .getContent();

        return new HelpdeskTicketContext(request, requester, responses);
    }
}