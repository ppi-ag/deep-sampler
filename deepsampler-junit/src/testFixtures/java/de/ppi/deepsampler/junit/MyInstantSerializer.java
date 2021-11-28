/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A simple {@link JsonSerializer} that is used to test the annotations {@link UseJsonSerializer} and {@link UseJsonDeserializer}
 * <p>
 * The serializer writes an {@link Instant} in a JSON-file and adds a suffix {@link MyInstantSerializer#TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED}
 * to the persistent Instant. This suffix is used by {@link MyInstantDeserializer} to ensure that the {@link Instant} was saved by this {@link JsonSerializer}
 * and not by the standard Jackson-Serializer or a standard {@link de.ppi.deepsampler.persistence.model.PersistentBean}.
 */
public class MyInstantSerializer extends JsonSerializer<Instant> {

    public static final String TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED = "_Test";
    public static final DateTimeFormatter ISO_LOCAL_DATE = DateTimeFormatter.ISO_LOCAL_DATE_TIME
            .withLocale(Locale.GERMANY)
            .withZone(ZoneId.of("UTC"));

    @Override
    public void serialize(final Instant value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
        final String formattedDate = ISO_LOCAL_DATE.format(value);
        gen.writeString(formattedDate + TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED);
    }

    @Override
    public void serializeWithType(final Instant value, final JsonGenerator gen, final SerializerProvider serializers, final TypeSerializer typeSer) throws IOException {
        final WritableTypeId typeIdDef = typeSer.writeTypePrefix(gen,
                typeSer.typeId(value, JsonToken.VALUE_EMBEDDED_OBJECT));
        serialize(value, gen, serializers);
        typeSer.writeTypeSuffix(gen, typeIdDef);
    }

}
