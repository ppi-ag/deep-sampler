/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

/**
 * A simple {@link JsonDeserializer} that is used to test the annotations {@link UseJsonSerializer} and {@link UseJsonDeserializer}
 * <p>
 * {@link MyInstantSerializer} writes an {@link Instant} in a JSON-file and adds a suffix {@link MyInstantSerializer#TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED}
 * to the persistent Instant. This suffix is used by this {@link MyInstantDeserializer} to ensure that the {@link Instant} was saved by {@link MyInstantSerializer}
 * and not by the standard Jackson-Serializer or a standard {@link de.ppi.deepsampler.persistence.model.PersistentBean}.
 */
public class MyInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (!p.getValueAsString().contains(MyInstantSerializer.TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED)) {
            throw new IllegalStateException("The deserialized value was not written by MyInstantSerializer, because the suffix "
                    + MyInstantSerializer.TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED
                    + " is missing.");
        }

        final String cleanedInstant = p.getValueAsString().replaceAll(MyInstantSerializer.TEST_SUFFIX_TO_ENSURE_THAT_SERIALIZER_WAS_USED, "");
        return Instant.from(MyInstantSerializer.ISO_LOCAL_DATE.parse(cleanedInstant));
    }
}
