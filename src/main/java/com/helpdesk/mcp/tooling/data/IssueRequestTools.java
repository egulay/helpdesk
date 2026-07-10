package com.helpdesk.mcp.tooling.data;

import com.helpdesk.data.model.IssueRequestModel;
import com.helpdesk.data.service.IssueRequestService;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import com.helpdesk.mcp.dto.IssueRequestToolRequest;
import com.helpdesk.mcp.dto.IssueRequestToolResponse;
import com.helpdesk.mcp.dto.PagedToolResponse;
import com.helpdesk.mcp.util.McpDateParser;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueRequestTools {

    private final IssueRequestService issueRequestService;
    private final IssueRequesterService issueRequesterService;

    @Tool(description = "Find issue request by id")
    public IssueRequestToolResponse findIssueRequestById(Integer id) {
        return toResponse(issueRequestService.findById(id));
    }

    @Tool(description = "Find issue request by id and solved status")
    public IssueRequestToolResponse findIssueRequestByIdAndSolvedStatus(Integer id, boolean isSolved) {
        return toResponse(issueRequestService.findById(id, isSolved));
    }

    @Tool(description = "Find all issue requests with pagination and sorting")
    public PagedToolResponse<IssueRequestToolResponse> findAllIssueRequests(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequestService.findAll(page, size, sortBy, sortDirection));
    }

    @Tool(description = "Find issue requests created between two dates. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public PagedToolResponse<IssueRequestToolResponse> findIssueRequestsByCreatedDateRange(
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequestService.findAllByCreatedBeforeAndCreatedAfter(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requests created between two dates and filtered by solved status. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public PagedToolResponse<IssueRequestToolResponse> findIssueRequestsByCreatedDateRangeAndSolvedStatus(
            String createdBefore,
            String createdAfter,
            boolean isSolved,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequestService.findAllByCreatedBeforeAndCreatedAfterAndIsSolved(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                isSolved,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requests solved between two dates. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public PagedToolResponse<IssueRequestToolResponse> findIssueRequestsBySolvedDateRange(
            String solvedBefore,
            String solvedAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequestService.findAllBySolvedBeforeAndSolvedAfter(
                McpDateParser.fromIsoInstant(solvedBefore),
                McpDateParser.fromIsoInstant(solvedAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requests by requester id")
    public PagedToolResponse<IssueRequestToolResponse> findIssueRequestsByRequesterId(
            Integer requesterId,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequestService.findAllByRequesterId(
                requesterId,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requests by requester id and created date range. Dates must be ISO-8601 instants (example: 2026-07-09T00:00:00Z).")
    public PagedToolResponse<IssueRequestToolResponse> findIssueRequestsByRequesterIdAndCreatedDateRange(
            Integer requesterId,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequestService.findAllByRequesterIdAndCreatedBeforeAndCreatedAfter(
                requesterId,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Mark an issue request as solved")
    public IssueRequestToolResponse solveIssueRequest(Integer id) {
        return toResponse(issueRequestService.solveIssue(id));
    }

    @Tool(description = "Check whether an issue request exists")
    public Boolean issueRequestExists(Integer id) {
        return issueRequestService.isExists(id);
    }

    @Tool(description = "Save or update an issue request")
    public IssueRequestToolResponse saveIssueRequest(IssueRequestToolRequest request) {
        return toResponse(issueRequestService.save(toModel(request)));
    }

    @Tool(description = "Hard delete an issue request by id")
    public IssueRequestToolResponse hardDeleteIssueRequest(Integer id) {
        return toResponse(issueRequestService.hardDelete(id));
    }

    private IssueRequestModel toModel(IssueRequestToolRequest request) {
        return IssueRequestModel
                .builder()
                .id(request.id())
                .requester(issueRequesterService.findById(request.requesterId()))
                .body(request.requestBody())
                .isSolved(request.isSolved())
                .solved(request.solved() == null ? null : McpDateParser.fromIsoInstant(request.solved()))
                .build();
    }

    private IssueRequestToolResponse toResponse(IssueRequestModel model) {
        return new IssueRequestToolResponse(
                model.getId(),
                model.getRequester().getId(),
                model.getBody(),
                model.getIsSolved(),
                model.getCreated() == null ? null : model.getCreated().toInstant().toString(),
                model.getSolved() == null ? null : model.getSolved().toInstant().toString()
        );
    }

    private PagedToolResponse<IssueRequestToolResponse> toPagedResponse(
            GenericPagedModel<IssueRequestModel> model
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