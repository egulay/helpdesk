package com.helpdesk.mcp.tools.data;

import com.helpdesk.mcp.dto.IssueRequestToolRequest;
import com.helpdesk.mcp.dto.IssueRequestToolResponse;
import com.helpdesk.mcp.dto.IssueRequesterToolRequest;
import com.helpdesk.mcp.dto.IssueRequesterToolResponse;
import com.helpdesk.mcp.dto.IssueResponseToolRequest;
import com.helpdesk.mcp.dto.IssueResponseToolResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        prefix = "helpdesk.mcp",
        name = "allow-mutations",
        havingValue = "true"
)
public class HelpdeskMutationTools {

    private final IssueRequesterTools requesterTools;
    private final IssueRequestTools requestTools;
    private final IssueResponseTools responseTools;

    @Tool(description = "Toggle activation status of an issue requester")
    public IssueRequesterToolResponse toggleIssueRequesterActivation(Integer id) {
        log.info("Calling MCP tool: toggleIssueRequesterActivation >> Requester Id: {}", id);
        return requesterTools.toggleIssueRequesterActivation(id);
    }

    @Tool(description = "Save or update an issue requester")
    public IssueRequesterToolResponse saveIssueRequester(IssueRequesterToolRequest request) {
        log.info("Calling MCP tool: saveIssueRequester >> Requester Id: {}", request.id());
        return requesterTools.saveIssueRequester(request);
    }

    @Tool(description = "Hard delete an issue requester by id")
    public IssueRequesterToolResponse hardDeleteIssueRequester(Integer id) {
        log.info("Calling MCP tool: hardDeleteIssueRequester >> Requester Id: {}", id);
        return requesterTools.hardDeleteIssueRequester(id);
    }

    @Tool(description = "Mark an issue request as solved")
    public IssueRequestToolResponse solveIssueRequest(Integer id) {
        log.info("Calling MCP tool: solveIssueRequest >> Request Id: {}", id);
        return requestTools.solveIssueRequest(id);
    }

    @Tool(description = "Save or update an issue request")
    public IssueRequestToolResponse saveIssueRequest(IssueRequestToolRequest request) {
        log.info("Calling MCP tool: saveIssueRequest >> Request Id: {}", request.id());
        return requestTools.saveIssueRequest(request);
    }

    @Tool(description = "Hard delete an issue request by id")
    public IssueRequestToolResponse hardDeleteIssueRequest(Integer id) {
        log.info("Calling MCP tool: hardDeleteIssueRequest >> Request Id: {}", id);
        return requestTools.hardDeleteIssueRequest(id);
    }

    @Tool(description = "Save or update an issue response")
    public IssueResponseToolResponse saveIssueResponse(IssueResponseToolRequest request) {
        log.info("Calling MCP tool: saveIssueResponse >> Response Id: {}", request.id());
        return responseTools.saveIssueResponse(request);
    }

    @Tool(description = "Hard delete an issue response by id")
    public IssueResponseToolResponse hardDeleteIssueResponse(Integer id) {
        log.info("Calling MCP tool: hardDeleteIssueResponse >> Response Id: {}", id);
        return responseTools.hardDeleteIssueResponse(id);
    }
}
