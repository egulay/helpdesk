package io.gulay.helpdesk.mcp.client;

import com.openai.client.OpenAIClient;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.services.blocking.ResponseService;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AbstractAiServiceTests {

    private static final class TestAiService extends AbstractAiService {
        private TestAiService(OpenAIClient client, String model) {
            super(client, model);
        }
    }

    private OpenAIClient client;
    private ResponseService responseService;
    private AiService aiService;

    @BeforeEach
    public void setup() {
        client = mock(OpenAIClient.class);
        responseService = mock(ResponseService.class);
        when(client.responses()).thenReturn(responseService);

        aiService = new TestAiService(client, "test-model");
    }

    private static ResponseOutputItem outputItemWith(String... texts) {
        val contents = Arrays.stream(texts)
                .map(text -> {
                    val outputText = mock(ResponseOutputText.class);
                    when(outputText.text()).thenReturn(text);

                    val content = mock(ResponseOutputMessage.Content.class);
                    when(content.outputText()).thenReturn(Optional.of(outputText));

                    return content;
                })
                .toList();

        val message = mock(ResponseOutputMessage.class);
        when(message.content()).thenReturn(contents);

        val item = mock(ResponseOutputItem.class);
        when(item.message()).thenReturn(Optional.of(message));

        return item;
    }

    private void stubProviderOutput(ResponseOutputItem... items) {
        val response = mock(Response.class);
        when(response.output()).thenReturn(List.of(items));
        when(responseService.create(any(ResponseCreateParams.class))).thenReturn(response);
    }

    @Test
    public void concatenates_output_text_segments_test() {
        stubProviderOutput(outputItemWith("Hello ", "world"));

        assertEquals("Hello world", aiService.chat("system prompt", "user prompt"));
    }

    @Test
    public void concatenates_text_across_output_items_test() {
        stubProviderOutput(outputItemWith("first "), outputItemWith("second"));

        assertEquals("first second", aiService.chat("system prompt", "user prompt"));
    }

    @Test
    public void skips_blank_output_segments_test() {
        stubProviderOutput(outputItemWith("   ", "real answer"));

        assertEquals("real answer", aiService.chat("system prompt", "user prompt"));
    }

    @Test
    public void throws_when_provider_returns_no_output_test() {
        stubProviderOutput();

        val ex = assertThrows(
                IllegalStateException.class,
                () -> aiService.chat("system prompt", "user prompt")
        );

        assertEquals("The AI provider returned no textual response.", ex.getMessage());
    }

    @Test
    public void throws_when_all_output_is_blank_test() {
        stubProviderOutput(outputItemWith("   "));

        assertThrows(
                IllegalStateException.class,
                () -> aiService.chat("system prompt", "user prompt")
        );
    }

    @Test
    public void sends_system_prompt_as_instructions_test() {
        stubProviderOutput(outputItemWith("answer"));

        aiService.chat("you are a helpdesk assistant", "summarize ticket 7");

        val captor = ArgumentCaptor.forClass(ResponseCreateParams.class);
        verify(responseService).create(captor.capture());

        val params = captor.getValue();
        assertEquals(Optional.of("you are a helpdesk assistant"), params.instructions());
        assertTrue(params.input().isPresent());
        assertTrue(params.model().toString().contains("test-model"));
    }

    @Test
    public void rejects_null_system_prompt_test() {
        val ex = assertThrows(
                IllegalArgumentException.class,
                () -> aiService.chat(null, "user prompt")
        );

        assertEquals("systemPrompt must not be null or blank.", ex.getMessage());
    }

    @Test
    public void rejects_blank_system_prompt_test() {
        assertThrows(IllegalArgumentException.class, () -> aiService.chat("   ", "user prompt"));
    }

    @Test
    public void rejects_null_user_prompt_test() {
        val ex = assertThrows(
                IllegalArgumentException.class,
                () -> aiService.chat("system prompt", null)
        );

        assertEquals("userPrompt must not be null or blank.", ex.getMessage());
    }

    @Test
    public void rejects_blank_user_prompt_test() {
        assertThrows(IllegalArgumentException.class, () -> aiService.chat("system prompt", ""));
    }

    @Test
    public void does_not_call_the_provider_for_invalid_prompts_test() {
        assertThrows(IllegalArgumentException.class, () -> aiService.chat("", ""));

        verifyNoInteractions(responseService);
    }

    @Test
    public void lm_studio_service_uses_the_shared_chat_pipeline_test() {
        stubProviderOutput(outputItemWith("lm studio answer"));

        val service = new LmStudioAiService(client, "qwen3-coder", "http://localhost:1234/v1");

        assertEquals("lm studio answer", service.chat("system prompt", "user prompt"));
    }

    @Test
    public void openai_service_uses_the_shared_chat_pipeline_test() {
        stubProviderOutput(outputItemWith("openai answer"));

        val service = new OpenAiService(client, "gpt-5.2", "https://api.openai.com/v1");

        assertEquals("openai answer", service.chat("system prompt", "user prompt"));
    }
}
