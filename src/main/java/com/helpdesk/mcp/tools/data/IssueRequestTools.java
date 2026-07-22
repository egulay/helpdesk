package com.helpdesk.mcp.tools.data;

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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IssueRequestTools {

    private final IssueRequestService issueRequestService;
    private final IssueRequesterService issueRequesterService;

    @Tool(description = "Find issue request by id")
    public IssueRequestToolResponse findIssueRequestById(Integer id) {
        log.info("Calling MCP tool: findIssueRequestById >> Request Id: {}", id);
        return toResponse(issueRequestService.findById(id));
    }

    @Tool(description = "Find issue request by id and solved status")
    public IssueRequestToolResponse findIssueRequestByIdAndSolvedStatus(Integer id, boolean isSolved) {
        log.info("Calling MCP tool: findIssueRequestByIdAndSolvedStatus >> Request Id: {}, Solved: {}", id, isSolved);
        return toResponse(issueRequestService.findById(id, isSolved));
    }

    @Tool(description = "Find all issue requests with pagination and sorting")
    public PagedToolResponse<IssueRequestToolResponse> findAllIssueRequests(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        log.info("Calling MCP tool: findAllIssueRequests >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestsByCreatedDateRange >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestsByCreatedDateRangeAndSolvedStatus >> Solved: {}, Page: {}, Size: {}", isSolved, page, size);
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
        log.info("Calling MCP tool: findIssueRequestsBySolvedDateRange >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestsByRequesterId >> Requester Id: {}, Page: {}, Size: {}", requesterId, page, size);
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
        log.info("Calling MCP tool: findIssueRequestsByRequesterIdAndCreatedDateRange >> Requester Id: {}, Page: {}, Size: {}", requesterId, page, size);
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

    public IssueRequestToolResponse solveIssueRequest(Integer id) {
        return toResponse(issueRequestService.solveIssue(id));
    }

    @Tool(description = "Check whether an issue request exists")
    public Boolean issueRequestExists(Integer id) {
        log.info("Calling MCP tool: issueRequestExists >> Request Id: {}", id);
        return issueRequestService.isExists(id);
    }

    public IssueRequestToolResponse saveIssueRequest(IssueRequestToolRequest request) {
        return toResponse(issueRequestService.save(toModel(request)));
    }

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
