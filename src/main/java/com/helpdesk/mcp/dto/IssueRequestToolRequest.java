package com.helpdesk.mcp.dto;

public record IssueRequestToolRequest(
        Integer id,
        Integer requesterId,
        String requestBody,
        Boolean isSolved
) {

}