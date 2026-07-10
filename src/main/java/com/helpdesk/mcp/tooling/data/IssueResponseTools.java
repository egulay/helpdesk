package com.helpdesk.mcp.tooling.data;

import com.helpdesk.data.model.IssueResponseModel;
import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.service.IssueResponseService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import com.helpdesk.mcp.dto.IssueResponseToolRequest;
import com.helpdesk.mcp.dto.IssueResponseToolResponse;
import com.helpdesk.mcp.dto.PagedToolResponse;
import com.helpdesk.mcp.util.McpDateParser;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueResponseTools {

    private final IssueResponseService issueResponseService;
    private final IssueRequestService issueRequestService;
    private final IssueRequesterService issueRequesterService;

    @Tool(description = "Find issue response by id")
    public IssueResponseToolResponse findIssueResponseById(Integer id) {
        return toResponse(issueResponseService.findById(id));
    }

    @Tool(description = "Find all issue responses with pagination and sorting")
    public PagedToolResponse<IssueResponseToolResponse> findAllIssueResponses(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueResponseService.findAll(page, size, sortBy, sortDirection));
    }

    @Tool(description = "Find issue responses created between two dates. Dates must be ISO-8601 instants, for example 2026-07-09T00:00:00Z.")
    public PagedToolResponse<IssueResponseToolResponse> findIssueResponsesByCreatedDateRange(
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueResponseService.findAllByCreatedBeforeAndCreatedAfter(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue responses by request id")
    public PagedToolResponse<IssueResponseToolResponse> findIssueResponsesByRequestId(
            Integer requestId,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueResponseService.findAllByRequestId(
                requestId,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue responses by request id and created date range. Dates must be ISO-8601 instants, for example 2026-07-09T00:00:00Z.")
    public PagedToolResponse<IssueResponseToolResponse> findIssueResponsesByRequestIdAndCreatedDateRange(
            Integer requestId,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueResponseService.findAllByRequestIdAndCreatedBeforeAndCreatedAfter(
                requestId,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue responses by requester id")
    public PagedToolResponse<IssueResponseToolResponse> findIssueResponsesByRequesterId(
            Integer requesterId,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueResponseService.findAllByRequesterId(
                requesterId,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue responses by requester id and created date range. Dates must be ISO-8601 instants, for example 2026-07-09T00:00:00Z.")
    public PagedToolResponse<IssueResponseToolResponse> findIssueResponsesByRequesterIdAndCreatedDateRange(
            Integer requesterId,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueResponseService.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
                requesterId,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Save or update an issue response")
    public IssueResponseToolResponse saveIssueResponse(IssueResponseToolRequest request) {
        return toResponse(issueResponseService.save(toModel(request)));
    }

    @Tool(description = "Hard delete an issue response by id")
    public IssueResponseToolResponse hardDeleteIssueResponse(Integer id) {
        return toResponse(issueResponseService.hardDelete(id));
    }

    private IssueResponseModel toModel(IssueResponseToolRequest request) {
        return IssueResponseModel
                .builder()
                .id(request.id())
                .request(issueRequestService.findById(request.requestId()))
                .requester(issueRequesterService.findById(request.requesterId()))
                .body(request.responseBody())
                .build();
    }

    private IssueResponseToolResponse toResponse(IssueResponseModel model) {
        return new IssueResponseToolResponse(
                model.getId(),
                model.getRequest().getId(),
                model.getRequester().getId(),
                model.getBody(),
                model.getCreated() == null ? null : model.getCreated().toInstant().toString()
        );
    }

    private PagedToolResponse<IssueResponseToolResponse> toPagedResponse(
            GenericPagedModel<IssueResponseModel> model
    ) {
        val content = model.getContent()
                .stream()
                .map(this::toResponse)
                .toList();

        return new PagedToolResponse<>(
                model.getTotalElements(),
                model.getNumberOfElements(),
                model.getTotalPages(),
                content
        );
    }
}