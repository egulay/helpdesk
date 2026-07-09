package com.helpdesk.mcp.tool;

import com.helpdesk.data.model.IssueResponseModel;
import com.helpdesk.data.service.IssueResponseService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import com.helpdesk.mcp.util.McpDateParser;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueResponseTools {

    private final IssueResponseService issueResponseService;

    @Tool(description = "Find issue response by id")
    public IssueResponseModel findIssueResponseById(Integer id) {
        return issueResponseService.findById(id);
    }

    @Tool(description = "Find all issue responses with pagination and sorting")
    public GenericPagedModel<IssueResponseModel> findAllIssueResponses(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueResponseService.findAll(page, size, sortBy, sortDirection);
    }

    @Tool(description = "Find issue responses created between two dates. Dates must be ISO-8601 instants, for example 2026-07-09T00:00:00Z.")
    public GenericPagedModel<IssueResponseModel> findIssueResponsesByCreatedDateRange(
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueResponseService.findAllByCreatedBeforeAndCreatedAfter(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue responses by request id")
    public GenericPagedModel<IssueResponseModel> findIssueResponsesByRequestId(
            Integer requestId,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueResponseService.findAllByRequestId(
                requestId,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue responses by request id and created date range. Dates must be ISO-8601 instants, for example 2026-07-09T00:00:00Z.")
    public GenericPagedModel<IssueResponseModel> findIssueResponsesByRequestIdAndCreatedDateRange(
            Integer requestId,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueResponseService.findAllByRequestIdAndCreatedBeforeAndCreatedAfter(
                requestId,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue responses by requester id")
    public GenericPagedModel<IssueResponseModel> findIssueResponsesByRequesterId(
            Integer requesterId,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueResponseService.findAllByRequesterId(
                requesterId,
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Find issue responses by requester id and created date range. Dates must be ISO-8601 instants, for example 2026-07-09T00:00:00Z.")
    public GenericPagedModel<IssueResponseModel> findIssueResponsesByRequesterIdAndCreatedDateRange(
            Integer requesterId,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return issueResponseService.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
                requesterId,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        );
    }

    @Tool(description = "Save or update an issue response")
    public IssueResponseModel saveIssueResponse(IssueResponseModel model) {
        return issueResponseService.save(model);
    }

    @Tool(description = "Hard delete an issue response by id")
    public IssueResponseModel hardDeleteIssueResponse(Integer id) {
        return issueResponseService.hardDelete(id);
    }
}