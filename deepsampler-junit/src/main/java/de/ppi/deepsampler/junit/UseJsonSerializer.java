package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonSerializer;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(UseJsonSerializers.class)
public @interface UseJsonSerializer {
    Class<? extends JsonSerializer<?>> serializer();
    Class<?> forType();
}
