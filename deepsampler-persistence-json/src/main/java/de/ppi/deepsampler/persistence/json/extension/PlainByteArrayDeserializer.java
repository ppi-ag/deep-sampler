/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json.extension;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
/**
 * This Deserializer deserializes a byte[], that was serialized as a list of integers.
 * This class comes in a bundle with {@link PlainByteArraySerializer}
 *
 */
public class PlainByteArrayDeserializer extends JsonDeserializer<byte[]> {

    @Override
    public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {

        ObjectCodec objectCodec = p.getCodec();
        JsonNode node = objectCodec.readTree(p);

        if (node.isArray() && node.isEmpty()) {
            return null;
        }

        byte[] deserializedBytes = new byte[node.size()];
        int i =0;
        for (JsonNode value : node) {
            deserializedBytes[i]= (byte) value.asInt();
            i++;
        }

        return deserializedBytes;
    }
}
