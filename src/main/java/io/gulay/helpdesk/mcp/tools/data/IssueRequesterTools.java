package io.gulay.helpdesk.mcp.tools.data;

import io.gulay.helpdesk.data.model.IssueRequesterModel;
import io.gulay.helpdesk.data.service.IssueRequesterService;
import io.gulay.helpdesk.data.util.GenericPagedModel;
import io.gulay.helpdesk.data.util.SortDirection;
import io.gulay.helpdesk.mcp.dto.IssueRequesterToolRequest;
import io.gulay.helpdesk.mcp.dto.IssueRequesterToolResponse;
import io.gulay.helpdesk.mcp.dto.PagedToolResponse;
import io.gulay.helpdesk.mcp.util.McpDateParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused") // Invoked reflectively through Spring AI @Tool metadata.
public class IssueRequesterTools {

    private final IssueRequesterService issueRequesterService;

    @Tool(description = "Find issue requester by id")
    public IssueRequesterToolResponse findIssueRequesterById(Integer id) {
        log.info("Calling MCP tool: findIssueRequesterById >> Requester Id: {}", id);
        return toResponse(issueRequesterService.findById(id));
    }

    @Tool(description = "Find issue requester by id and active status")
    public IssueRequesterToolResponse findIssueRequesterByIdAndActiveStatus(Integer id, boolean isActive) {
        log.info("Calling MCP tool: findIssueRequesterByIdAndActiveStatus >> Requester Id: {}, Active: {}", id, isActive);
        return toResponse(issueRequesterService.findById(id, isActive));
    }

    @Tool(description = "Find all issue requesters with pagination and sorting")
    public PagedToolResponse<IssueRequesterToolResponse> findAllIssueRequesters(
            int page,
            int size,
            String sortBy,
            SortDirection sortDirection
    ) {
        log.info("Calling MCP tool: findAllIssueRequesters >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestersByFullName >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestersByFullNameAndCreatedDateRange >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestersByEmail >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestersByEmailAndCreatedDateRange >> Page: {}, Size: {}", page, size);
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
        log.info("Calling MCP tool: findIssueRequestersByCreatedDateRangeAndActiveStatus >> Active: {}, Page: {}, Size: {}", isActive, page, size);
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
        log.info("Calling MCP tool: findIssueRequestersByCreatedDateRange >> Page: {}, Size: {}", page, size);
        return toPagedResponse(issueRequesterService.findAllByCreatedBeforeAndCreatedAfter(
                McpDateParser.fromIsoInstant(createdBefore),
                McpDateParser.fromIsoInstant(createdAfter),
                page,
                size,
                sortBy,
                sortDirection
        ));
    }

    public IssueRequesterToolResponse toggleIssueRequesterActivation(Integer id) {
        return toResponse(issueRequesterService.toggleActivation(id));
    }

    @Tool(description = "Check whether an issue requester exists with active status")
    public Boolean issueRequesterExistsAndActive(Integer id, Boolean isActive) {
        log.info("Calling MCP tool: issueRequesterExistsAndActive >> Requester Id: {}, Active: {}", id, isActive);
        return issueRequesterService.isExistsAndActive(id, isActive);
    }

    public IssueRequesterToolResponse saveIssueRequester(IssueRequesterToolRequest request) {
        return toResponse(issueRequesterService.save(toModel(request)));
    }

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
