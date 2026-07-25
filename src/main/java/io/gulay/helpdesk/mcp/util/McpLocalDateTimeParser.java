package io.gulay.helpdesk.mcp.util;

import java.time.Instant;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class McpLocalDateTimeParser {
    public static LocalDateTime fromIsoInstant(String value) {
        return LocalDateTime.ofInstant(
                Instant.parse(value),
                ZoneOffset.UTC
        );
    }

}
