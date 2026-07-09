package com.helpdesk.mcp.dto;

public record IssueResponseToolRequest(
        Integer id,
        Integer requestId,
        Integer requesterId,
        String responseBody
) {
}