package com.helpdesk.mcp.tools.data;

import com.helpdesk.data.model.IssueRequesterModel;
import com.helpdesk.data.service.IssueRequesterService;
import com.helpdesk.data.util.GenericPagedModel;
import com.helpdesk.data.util.SortDirection;
import com.helpdesk.mcp.dto.IssueRequesterToolRequest;
import com.helpdesk.mcp.dto.IssueRequesterToolResponse;
import com.helpdesk.mcp.dto.PagedToolResponse;
import com.helpdesk.mcp.util.McpDateParser;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IssueRequesterTools {

    private final IssueRequesterService issueRequesterService;

    @Tool(description = "Find issue requester by id")
    public IssueRequesterToolResponse findIssueRequesterById(Integer id) {
        return toResponse(issueRequesterService.findById(id));
    }

    @Tool(description = "Find issue requester by id and active status")
    public IssueRequesterToolResponse findIssueRequesterByIdAndActiveStatus(Integer id, boolean isActive) {
        return toResponse(issueRequesterService.findById(id, isActive));
    }

    @Tool(description = "Find all issue requesters with pagination and sorting")
    public PagedToolResponse<IssueRequesterToolResponse> findAllIssueRequesters(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAll(page, size, sortBy, sortDirection));
    }

    @Tool(description = "Find issue requesters by full name")
    public PagedToolResponse<IssueRequesterToolResponse> findIssueRequestersByFullName(
            String fullName,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAllByFullNameContainingIgnoreCase(
                fullName,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requesters by full name and created date range. Date format must be ISO-8601 instant, for example 2026-07-09T00:00:00Z")
    public PagedToolResponse<IssueRequesterToolResponse> findIssueRequestersByFullNameAndCreatedDateRange(
            String fullName,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAllByFullNameContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
                fullName,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requesters by email")
    public PagedToolResponse<IssueRequesterToolResponse> findIssueRequestersByEmail(
            String email,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAllByEmailContainingIgnoreCase(
                email,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requesters by email and created date range. Date format must be ISO-8601 instant, for example 2026-07-09T00:00:00Z")
    public PagedToolResponse<IssueRequesterToolResponse> findIssueRequestersByEmailAndCreatedDateRange(
            String email,
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAllByEmailContainingIgnoreCaseAndCreatedBeforeAndCreatedAfter(
                email,
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requesters by created date range and active status. Date format must be ISO-8601 instant, for example 2026-07-09T00:00:00Z")
    public PagedToolResponse<IssueRequesterToolResponse> findIssueRequestersByCreatedDateRangeAndActiveStatus(
            String createdBefore,
            String createdAfter,
            boolean isActive,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAllByCreatedBeforeAndCreatedAfterAndIsActive(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                isActive,
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Find issue requesters by created date range. Date format must be ISO-8601 instant, for example 2026-07-09T00:00:00Z")
    public PagedToolResponse<IssueRequesterToolResponse> findIssueRequestersByCreatedDateRange(
            String createdBefore,
            String createdAfter,
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        return toPagedResponse(issueRequesterService.findAllByCreatedBeforeAndCreatedAfter(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    @Tool(description = "Toggle activation status of an issue requester")
    public IssueRequesterToolResponse toggleIssueRequesterActivation(Integer id) {
        return toResponse(issueRequesterService.toggleActivation(id));
    }

    @Tool(description = "Check whether an issue requester exists with active status")
    public Boolean issueRequesterExistsAndActive(Integer id, Boolean isActive) {
        return issueRequesterService.isExistsAndActive(id, isActive);
    }

    @Tool(description = "Save or update an issue requester")
    public IssueRequesterToolResponse saveIssueRequester(IssueRequesterToolRequest request) {
        return toResponse(issueRequesterService.save(toModel(request)));
    }

    @Tool(description = "Hard delete an issue requester by id")
    public IssueRequesterToolResponse hardDeleteIssueRequester(Integer id) {
        return toResponse(issueRequesterService.hardDelete(id));
    }

    private IssueRequesterModel toModel(IssueRequesterToolRequest request) {
        return IssueRequesterModel
                .builder()
                .id(request.id())
                .fullName(request.fullName())
                .email(request.email())
                .isActive(request.isActive())
                .build();
    }

    private IssueRequesterToolResponse toResponse(IssueRequesterModel model) {
        return new IssueRequesterToolResponse(
                model.getId(),
                model.getFullName(),
                model.getEmail(),
                model.getIsActive(),
                model.getCreated() == null ? null : model.getCreated().toInstant().toString()
        );
    }

    private PagedToolResponse<IssueRequesterToolResponse> toPagedResponse(
            GenericPagedModel<IssueRequesterModel> model
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
