package io.gulay.helpdesk.controller.util;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class ParsersTests {

    @Test
    public void parses_boolean_aliases_test() {
        assertTrue(Parsers.tryParseBoolean("true", "isActive"));
        assertTrue(Parsers.tryParseBoolean("TRUE", "isActive"));
        assertTrue(Parsers.tryParseBoolean("yes", "isActive"));
        assertTrue(Parsers.tryParseBoolean("on", "isActive"));
        assertFalse(Parsers.tryParseBoolean("false", "isActive"));
        assertFalse(Parsers.tryParseBoolean("no", "isActive"));
        assertFalse(Parsers.tryParseBoolean("off", "isActive"));
    }

    @Test
    public void rejects_unparsable_boolean_as_not_acceptable_test() {
        val ex = assertThrows(
                ResponseStatusException.class,
                () -> Parsers.tryParseBoolean("maybe", "isActive")
        );

        assertEquals(HttpStatus.NOT_ACCEPTABLE, ex.getStatusCode());
        assertEquals("isActive:maybe", ex.getReason());
    }

    @Test
    public void parses_integer_test() {
        assertEquals(12, Parsers.tryParseInteger("12", "requesterId"));
    }

    @Test
    public void rejects_unparsable_integer_as_not_acceptable_test() {
        val ex = assertThrows(
                ResponseStatusException.class,
                () -> Parsers.tryParseInteger("abc", "requesterId")
        );

        assertEquals(HttpStatus.NOT_ACCEPTABLE, ex.getStatusCode());
        assertEquals("requesterId:abc", ex.getReason());
    }

    @Test
    public void rejects_decimal_integer_test() {
        assertThrows(
                ResponseStatusException.class,
                () -> Parsers.tryParseInteger("3.5", "requesterId")
        );
    }

    @Test
    public void treats_minus_one_as_unparsable_integer_test() {
        // -1 is the sentinel the parser uses for failure, so a literal -1 is rejected too.
        assertThrows(
                ResponseStatusException.class,
                () -> Parsers.tryParseInteger("-1", "requesterId")
        );
    }

    @Test
    public void parses_long_test() {
        assertEquals(1791873915466L, Parsers.tryParseLong("1791873915466", "created"));
    }

    @Test
    public void rejects_unparsable_long_as_not_acceptable_test() {
        val ex = assertThrows(
                ResponseStatusException.class,
                () -> Parsers.tryParseLong("not-a-date", "created")
        );

        assertEquals(HttpStatus.NOT_ACCEPTABLE, ex.getStatusCode());
        assertEquals("created:not-a-date", ex.getReason());
    }
}
