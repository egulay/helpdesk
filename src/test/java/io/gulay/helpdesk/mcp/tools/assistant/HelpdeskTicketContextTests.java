package io.gulay.helpdesk.mcp.tools.assistant;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HelpdeskTicketContextTests {

    private static HelpdeskTicketContext.TicketRequester requester() {
        return new HelpdeskTicketContext.TicketRequester(
                1, "Ada Lovelace", "ada@example.com", true, "2026-07-09T10:15:30Z"
        );
    }

    private static HelpdeskTicketContext.TicketRequest request() {
        return new HelpdeskTicketContext.TicketRequest(
                7, 1, "Printer is offline", false, "2026-07-09T10:15:30Z", null
        );
    }

    private static HelpdeskTicketContext.TicketResponse response(Integer id, String body) {
        return new HelpdeskTicketContext.TicketResponse(id, 7, 1, body, "2026-07-09T11:00:00Z");
    }

    @Test
    public void prompt_text_contains_requester_request_and_responses_test() {
        val context = new HelpdeskTicketContext(
                request(),
                requester(),
                List.of(response(3, "Rebooted the spooler"), response(4, "Replaced the cable"))
        );

        val text = context.toPromptText();

        assertTrue(text.contains("Requester:"));
        assertTrue(text.contains("Request:"));
        assertTrue(text.contains("Responses:"));
        assertTrue(text.contains("Ada Lovelace"));
        assertTrue(text.contains("ada@example.com"));
        assertTrue(text.contains("Printer is offline"));
        assertTrue(text.contains("Rebooted the spooler"));
        assertTrue(text.contains("Replaced the cable"));
    }

    @Test
    public void prompt_text_marks_empty_response_history_test() {
        val context = new HelpdeskTicketContext(request(), requester(), List.of());

        assertTrue(context.toPromptText().contains("No responses found."));
    }

    @Test
    public void prompt_text_tolerates_null_response_collection_test() {
        val context = new HelpdeskTicketContext(request(), requester(), null);

        assertTrue(context.toPromptText().contains("No responses found."));
    }

    @Test
    public void prompt_text_renders_missing_values_explicitly_test() {
        val context = new HelpdeskTicketContext(request(), requester(), List.of());

        // An unsolved request has no solved timestamp; the prompt must still show the field.
        assertTrue(context.toPromptText().contains("solved: null"));
    }

    @Test
    public void response_prompt_text_is_bulleted_per_response_test() {
        val context = new HelpdeskTicketContext(
                request(),
                requester(),
                List.of(response(3, "first"), response(4, "second"))
        );

        val bullets = context.toPromptText().lines().filter(line -> line.startsWith("- ")).count();
        assertEquals(2, bullets);
    }
}
