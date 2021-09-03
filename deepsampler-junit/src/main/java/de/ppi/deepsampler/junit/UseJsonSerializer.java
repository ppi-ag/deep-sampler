package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonSerializer;

public @interface UseJsonSerializer {
    Class<JsonSerializer<?>>[] value();
}
