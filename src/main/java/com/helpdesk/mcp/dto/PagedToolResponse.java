package com.helpdesk.mcp.dto;

import java.util.Collection;

public record PagedToolResponse<T>(
        long totalElements,
        int numberOfElements,
        int totalPages,
        Collection<T> content
) {
}