package io.gulay.helpdesk.mcp;

import io.gulay.helpdesk.TestBase;
import io.gulay.helpdesk.mcp.client.AiService;
import io.gulay.helpdesk.mcp.client.DisabledAiService;
import lombok.val;
import org.junit.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class McpConfigurationIntegrationTests extends TestBase {

    @Autowired
    private AiService aiService;

    @Autowired
    private ToolCallbackProvider toolCallbackProvider;

    @Test
    public void disabled_ai_provider_does_not_create_external_client() {
        assertTrue(aiService instanceof DisabledAiService);
    }

    @Test
    public void disabled_ai_provider_rejects_assistant_requests_with_clear_reason() {
        try {
            aiService.chat("system prompt", "user prompt");
            fail("Expected disabled AI service to reject the request");
        } catch (IllegalStateException exception) {
            assertEquals(
                    "AI assistant tools are disabled by helpdesk.ai.enabled=false",
                    exception.getMessage()
            );
        }
    }

    @Test
    public void mcp_tools_are_read_only_by_default() {
        Set<String> toolNames = Arrays.stream(toolCallbackProvider.getToolCallbacks())
                .map(callback -> callback.getToolDefinition().name())
                .collect(Collectors.toSet());

        assertTrue(toolNames.contains("findIssueRequesterById"));
        assertTrue(toolNames.contains("findIssueRequestById"));
        assertTrue(toolNames.contains("findIssueResponseById"));

        assertFalse(toolNames.contains("saveIssueRequester"));
        assertFalse(toolNames.contains("hardDeleteIssueRequester"));
        assertFalse(toolNames.contains("solveIssueRequest"));
        assertFalse(toolNames.contains("saveIssueRequest"));
        assertFalse(toolNames.contains("hardDeleteIssueRequest"));
        assertFalse(toolNames.contains("saveIssueResponse"));
        assertFalse(toolNames.contains("hardDeleteIssueResponse"));
    }

    @Test
    public void mcp_uses_streamable_http_endpoint() {
        McpResponse initialize = postMcp(null, """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "initialize",
                  "params": {
                    "protocolVersion": "2025-03-26",
                    "capabilities": {},
                    "clientInfo": {
                      "name": "helpdesk-integration-test",
                      "version": "1.0.0"
                    }
                  }
                }
                """);

        assertEquals(200, initialize.status());
        assertNotNull(initialize.sessionId());
        assertFalse(initialize.sessionId().isBlank());
        assertTrue(initialize.body().contains("protocolVersion"));

        McpResponse initialized = postMcp(initialize.sessionId(), """
                {
                  "jsonrpc": "2.0",
                  "method": "notifications/initialized",
                  "params": {}
                }
                """);
        assertTrue(initialized.status() == 200 || initialized.status() == 202);

        McpResponse tools = postMcp(initialize.sessionId(), """
                {
                  "jsonrpc": "2.0",
                  "id": 2,
                  "method": "tools/list",
                  "params": {}
                }
                """);

        assertEquals(200, tools.status());
        assertTrue(tools.body().contains("findIssueRequesterById"));

        McpResponse toolCall = postMcp(initialize.sessionId(), """
                {
                  "jsonrpc": "2.0",
                  "id": 3,
                  "method": "tools/call",
                  "params": {
                    "name": "issueRequesterExistsAndActive",
                    "arguments": {
                      "id": 999999,
                      "isActive": true
                    }
                  }
                }
                """);

        assertEquals(200, toolCall.status());
        assertTrue(toolCall.body().contains("false"));

        val legacySseStatus = webClientBuilder.baseUrl("http://localhost:" + port)
                .build()
                .get()
                .uri("/sse")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchangeToMono(response -> response.releaseBody().thenReturn(response.statusCode().value()))
                .block();

        assertNotNull(legacySseStatus);
        assertEquals(404, legacySseStatus.intValue());
    }

    private McpResponse postMcp(String sessionId, String payload) {
        return webClientBuilder.baseUrl("http://localhost:" + port)
                .build()
                .post()
                .uri("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .headers(headers -> {
                    if (sessionId != null) {
                        headers.set("Mcp-Session-Id", sessionId);
                    }
                })
                .bodyValue(payload)
                .exchangeToMono(response -> {
                    int status = response.statusCode().value();
                    HttpHeaders headers = response.headers().asHttpHeaders();
                    String responseSessionId = headers.getFirst("Mcp-Session-Id");
                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("")
                            .map(body -> new McpResponse(status, responseSessionId, body));
                })
                .block();
    }

    private record McpResponse(int status, String sessionId, String body) {
    }
}
