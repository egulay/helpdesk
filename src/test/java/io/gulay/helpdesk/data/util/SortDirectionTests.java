package io.gulay.helpdesk.data.util;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

public class SortDirectionTests {

    @Test
    public void resolves_ascending_aliases_test() {
        assertEquals(SortDirection.Ascending, SortDirection.of("ascending"));
        assertEquals(SortDirection.Ascending, SortDirection.of("asc"));
        assertEquals(SortDirection.Ascending, SortDirection.of("a"));
    }

    @Test
    public void resolves_descending_aliases_test() {
        assertEquals(SortDirection.Descending, SortDirection.of("descending"));
        assertEquals(SortDirection.Descending, SortDirection.of("desc"));
        assertEquals(SortDirection.Descending, SortDirection.of("dsc"));
        assertEquals(SortDirection.Descending, SortDirection.of("d"));
    }

    @Test
    public void resolves_aliases_case_insensitively_test() {
        assertEquals(SortDirection.Ascending, SortDirection.of("ASC"));
        assertEquals(SortDirection.Descending, SortDirection.of("Desc"));
    }

    @Test
    public void rejects_blank_alias_as_bad_request_test() {
        val ex = assertThrows(ResponseStatusException.class, () -> SortDirection.of("   "));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertEquals("SortDirection cannot be empty", ex.getReason());
    }

    @Test
    public void rejects_null_alias_as_bad_request_test() {
        assertThrows(ResponseStatusException.class, () -> SortDirection.of(null));
    }

    @Test
    public void rejects_unknown_alias_as_bad_request_test() {
        val ex = assertThrows(ResponseStatusException.class, () -> SortDirection.of("sideways"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("sideways"));
    }
}
