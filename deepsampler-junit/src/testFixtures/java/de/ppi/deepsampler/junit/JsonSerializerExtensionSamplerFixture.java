package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import de.ppi.deepsampler.core.api.PersistentSample;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@UseJsonDeserializer(forType = Instant.class, deserializer = JsonSerializerExtensionSamplerFixture.MyInstantDeserializer.class)
@UseJsonSerializer(forType = Instant.class, serializer = JsonSerializerExtensionSamplerFixture.MyInstantSerializer.class)
public class JsonSerializerExtensionSamplerFixture implements SamplerFixture {

    @PrepareSampler
    private TestService testServiceSampler;

    @Override
    public void defineSamplers() {
        PersistentSample.of(testServiceSampler.getDate());
    }

    public static class MyInstantDeserializer extends JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return Instant.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(p.getValueAsString()));
        }
    }

    public static class MyInstantSerializer extends JsonSerializer<Instant> {

        @Override
        public void serialize(Instant value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            String formattedDate = DateTimeFormatter.ISO_LOCAL_DATE.format(value);
            gen.writeString(formattedDate);
        }

        @Override
        public void serializeWithType(Instant value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
            WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
                    typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
            serialize(value, gen, serializers);
            typeSer.writeTypeSuffix(gen, typeIdDef);
        }

    }
}
