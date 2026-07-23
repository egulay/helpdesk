package io.gulay.helpdesk.controller.util;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.springframework.boot.jackson.JacksonComponent;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.exc.StreamWriteException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

@JacksonComponent
public class ProtobufJsonSerializer extends ValueSerializer<Message> {
    @Override
    public void serialize(Message message, JsonGenerator gen, SerializationContext context) throws JacksonException {
        try {
            gen.writeRawValue(JsonFormat.printer().print(message));
        } catch (InvalidProtocolBufferException ex) {
            throw new StreamWriteException(gen, "Could not serialize Protobuf message", ex);
        }
    }
}
