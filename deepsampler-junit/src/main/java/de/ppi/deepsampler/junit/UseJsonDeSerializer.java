package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;

public @interface UseJsonDeSerializer {
    Class<JsonDeserializer<?>>[] value();
}
