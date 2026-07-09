package com.helpdesk.mcp.dto;

public record IssueRequesterToolRequest(
        Integer id,
        String fullName,
        String email,
        Boolean isActive
) {
}