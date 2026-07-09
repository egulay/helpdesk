package com.helpdesk.mcp.tool;

import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import com.helpdesk.mcp.util.McpDateParser;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueRequestTools {

    private final IssueRequestService issueRequestService;

    @Tool(description = "Find issue request by id")
    public IssueRequestModel findIssueRequestById(Integer id) {
        return issueRequestService.findById(id);
    }

    @Tool(description = "Find issue request by id and solved status")
    public IssueRequestModel findIssueRequestByIdAndSolvedStatus(Integer id, boolean isSolved) {
        return issueRequestService.findById(id, isSolved);
    }

    @Tool(description = "Find all issue requests with pagination and sorting")
    public GenericPagedModel<IssueRequestModel> findAllIssueRequests(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueRequestService.findAll(page, size, sortBy, sortDirection);
    }

    @Tool(description = "Find issue requests created between two dates. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public GenericPagedModel<IssueRequestModel> findIssueRequestsByCreatedDateRange(
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueRequestService.findAllByCreatedBeforeAndCreatedAfter(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue requests created between two dates and filtered by solved status. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public GenericPagedModel<IssueRequestModel> findIssueRequestsByCreatedDateRangeAndSolvedStatus(
            String createdBefore,
            String createdAfter,
            boolean isSolved,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueRequestService.findAllByCreatedBeforeAndCreatedAfterAndIsSolved(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                isSolved,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue requests solved between two dates. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public GenericPagedModel<IssueRequestModel> findIssueRequestsBySolvedDateRange(
            String solvedBefore,
            String solvedAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueRequestService.findAllBySolvedBeforeAndSolvedAfter(
                McpDateParser.fromIsoInstant(solvedBefore),
                McpDateParser.fromIsoInstant(solvedAfter),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue requests by requester id")
    public GenericPagedModel<IssueRequestModel> findIssueRequestsByRequesterId(
            Integer requesterId,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueRequestService.findAllByRequesterId(
                requesterId,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue requests by requester id and created date range. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public GenericPagedModel<IssueRequestModel> findIssueRequestsByRequesterIdAndCreatedDateRange(
            Integer requesterId,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueRequestService.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
                requesterId,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Mark an issue request as solved")
    public IssueRequestModel solveIssueRequest(Integer id) {
        return issueRequestService.solveIssue(id);
    }

    @Tool(description = "Check whether an issue request exists")
    public Boolean issueRequestExists(Integer id) {
        return issueRequestService.isExists(id);
    }

    @Tool(description = "Save or update an issue request")
    public IssueRequestModel saveIssueRequest(IssueRequestModel model) {
        return issueRequestService.save(model);
    }

    @Tool(description = "Hard delete an issue request by id")
    public IssueRequestModel hardDeleteIssueRequest(Integer id) {
        return issueRequestService.hardDelete(id);
    }
}