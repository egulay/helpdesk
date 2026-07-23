package io.gulay.helpdesk.mcp.util;

import java.time.Instant;

import java.util.Date;

public final class McpDateParser {
    public static Date fromIsoInstant(String value) {
        return Date.from(Instant.parse(value));
    }
}
