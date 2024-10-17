package org.example.ruleengine.serializer;
import java.io.IOException;

import org.example.ruleengine.model.Rule;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class RuleSerializer extends StdSerializer<Rule> {

    public RuleSerializer() {
        this(null);
    }

    public RuleSerializer(Class<Rule> t) {
        super(t);
    }

    @Override
    public void serialize(Rule rule, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("id", rule.getId());
        jsonGenerator.writeStringField("name", rule.getName());
        jsonGenerator.writeObjectField("rootNode", rule.getRootNode());
        jsonGenerator.writeEndObject();
    }
}
