package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
/**
 * This Deserializer deserialize a byte[] serialized as list of integer.
 * This class is a bundle with {@link PlainByteArraySerializer}
 *
 */
public class PlainByteArrayDeserializer extends JsonDeserializer<byte[]> {
    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        ObjectCodec oc = p.getCodec();
        JsonNode node = oc.readTree(p);

        if (node.isArray() && node.isEmpty()) {
            return null;
        }

        byte[] returnValue = new byte[node.size()];
        int i =0;
        for (JsonNode value : node) {
            returnValue[i]= (byte) value.asInt();
            i++;
        }

        return returnValue;
    }
}
