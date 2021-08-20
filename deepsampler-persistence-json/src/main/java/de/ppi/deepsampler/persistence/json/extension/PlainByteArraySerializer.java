package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * This Serializer serialize byte[] in a list of integer for easier debugging.
 * Jackson would otherwise serialize a byte[] as base64-String.
 *  This class is a bundle with {@link PlainByteArrayDeserializer}
 */
public class PlainByteArraySerializer extends StdSerializer<byte[]> {


    public PlainByteArraySerializer() {
        super(byte[].class);
    }

    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartArray();
        for (byte b: value) {
            gen.writeNumber(b );
        }
        gen.writeEndArray();
    }

    @Override
    public void serializeWithType(byte[] value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        // most likely scalar
        WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
                typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
        serialize(value,gen,serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }
}
