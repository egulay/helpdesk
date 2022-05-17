package com.helpdesk.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;

@JsonComponent
public class ProtobufJsonSerializer extends JsonSerializer<Message> {
    @Override
    public void serialize(Message message, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeRawValue(JsonFormat.printer().print(message));
    }
}
