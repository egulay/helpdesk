package com.helpdesk.mcp.dto;

public record IssueRequestToolResponse(
        Integer id,
        Integer requesterId,
        String requestBody,
        Boolean isSolved,
        String created,
        String solved
) {
}