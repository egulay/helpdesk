package io.gulay.helpdesk.mcp.config;

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenAiConfigurationTests {

    private final OpenAiConfiguration configuration = new OpenAiConfiguration();

    @Test
    public void open_ai_client_requires_an_api_key_test() {
        val ex = assertThrows(
                IllegalStateException.class,
                () -> configuration.openAiClient("", "https://api.openai.com/v1")
        );

        assertEquals(
                "Missing valid OpenAI API key from Vault property: helpdesk.ai.openai.api-key",
                ex.getMessage()
        );
    }

    @Test
    public void open_ai_client_rejects_null_api_key_test() {
        assertThrows(
                IllegalStateException.class,
                () -> configuration.openAiClient(null, "https://api.openai.com/v1")
        );
    }

    @Test
    public void open_ai_client_rejects_blank_api_key_test() {
        assertThrows(
                IllegalStateException.class,
                () -> configuration.openAiClient("   ", "https://api.openai.com/v1")
        );
    }

    @Test
    public void open_ai_client_is_built_with_a_valid_api_key_test() {
        assertNotNull(configuration.openAiClient("sk-test-key", "https://api.openai.com/v1"));
    }

    @Test
    public void lm_studio_client_needs_no_api_key_test() {
        // LM Studio is OpenAI-compatible but unauthenticated, so a placeholder key is used.
        assertNotNull(configuration.lmStudioClient("http://localhost:1234/v1"));
    }
}
