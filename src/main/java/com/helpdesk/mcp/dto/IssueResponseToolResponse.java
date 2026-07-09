package com.helpdesk.mcp.dto;

public record IssueResponseToolResponse(
        Integer id,
        Integer requestId,
        Integer requesterId,
        String responseBody,
        String created
) {
}