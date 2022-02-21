/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.persistence.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.ppi.deepsampler.persistence.json.extension.PlainByteArrayDeserializer;
import de.ppi.deepsampler.persistence.json.extension.PlainByteArraySerializer;

/**
 * This class bundles convenient Serializers for DeepSampler.
 *
 */
public class DeepSamplerSpecificModule extends SimpleModule {

    public DeepSamplerSpecificModule(){
        addSerializer(byte[].class, new PlainByteArraySerializer());
        addDeserializer(byte[].class, new PlainByteArrayDeserializer());

    }

}
