/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.PersistentSample;

import java.time.Instant;

/**
 * A simple {@link SamplerFixture} that is used to test the annotations {@link UseJsonSerializer} and {@link UseJsonDeserializer}
 */
@UseJsonDeserializer(forType = Instant.class, deserializer = MyInstantDeserializer.class)
@UseJsonSerializer(forType = Instant.class, serializer = MyInstantSerializer.class)
public class JsonSerializerExtensionSamplerFixture implements SamplerFixture {


    @PrepareSampler
    private TestService testServiceSampler;

    @Override
    public void defineSamplers() {
        PersistentSample.of(testServiceSampler.getInstant());
    }

}