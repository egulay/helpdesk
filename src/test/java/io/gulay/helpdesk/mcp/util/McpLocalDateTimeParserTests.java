package io.gulay.helpdesk.mcp.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

public class McpLocalDateTimeParserTests {

    @Test
    public void parses_iso_instant_as_utc_local_date_time_test() {
        assertEquals(
                LocalDateTime.of(2026, 7, 9, 10, 15, 30),
                McpLocalDateTimeParser.fromIsoInstant("2026-07-09T10:15:30Z")
        );
    }

    @Test
    public void normalizes_offset_instant_to_utc_test() {
        assertEquals(
                LocalDateTime.of(2026, 7, 9, 10, 15, 30),
                McpLocalDateTimeParser.fromIsoInstant("2026-07-09T13:15:30+03:00")
        );
    }

    @Test
    public void keeps_fractional_seconds_test() {
        assertEquals(
                123456000,
                McpLocalDateTimeParser.fromIsoInstant("2026-07-09T10:15:30.123456Z").getNano()
        );
    }

    @Test
    public void rejects_local_date_time_without_zone_test() {
        assertThrows(
                DateTimeParseException.class,
                () -> McpLocalDateTimeParser.fromIsoInstant("2026-07-09T10:15:30")
        );
    }

    @Test
    public void rejects_blank_value_test() {
        assertThrows(DateTimeParseException.class, () -> McpLocalDateTimeParser.fromIsoInstant(""));
    }

    @Test
    public void rejects_null_value_test() {
        assertThrows(NullPointerException.class, () -> McpLocalDateTimeParser.fromIsoInstant(null));
    }
}
