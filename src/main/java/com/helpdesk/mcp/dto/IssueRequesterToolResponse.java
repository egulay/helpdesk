package com.helpdesk.mcp.dto;

public record IssueRequesterToolResponse(
        Integer id,
        String fullName,
        String email,
        Boolean isActive,
        String created
) {
}