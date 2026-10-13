package io.gulay.helpdesk.controller.util;

import com.google.protobuf.Message;
import io.gulay.helpdesk.protoGen.ApiError;
import lombok.val;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import static org.junit.jupiter.api.Assertions.*;

public class ProtobufJsonSerializerTests {

    private final JsonMapper mapper = JsonMapper.builder()
            .addModule(new SimpleModule()
                    .addSerializer(Message.class, new ProtobufJsonSerializer()))
            .build();

    @Test
    public void writes_protobuf_message_as_canonical_protobuf_json_test() {
        val error = ApiError.newBuilder()
                .setStatus(404)
                .setError("Not Found")
                .setMessage("requesterId:999999")
                .setPath("/api/v1/issue-requesters/999999")
                .setTimestamp(1791873915466L)
                .build();

        val json = mapper.writeValueAsString(error);

        assertTrue(json.contains("\"status\": 404"));
        assertTrue(json.contains("\"error\": \"Not Found\""));
        assertTrue(json.contains("\"message\": \"requesterId:999999\""));
        assertTrue(json.contains("\"path\": \"/api/v1/issue-requesters/999999\""));
        // 64-bit protobuf values are rendered as strings by the canonical JSON mapping.
        assertTrue(json.contains("\"timestamp\": \"1791873915466\""));
    }

    @Test
    public void omits_unset_fields_test() {
        val json = mapper.writeValueAsString(ApiError.newBuilder().setStatus(500).build());

        assertTrue(json.contains("\"status\": 500"));
        assertFalse(json.contains("\"path\""));
    }
}
