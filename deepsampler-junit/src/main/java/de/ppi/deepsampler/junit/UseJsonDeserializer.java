package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;

import javax.swing.text.Element;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(UseJsonDeserializers.class)
public @interface UseJsonDeserializer {
    Class<? extends JsonDeserializer<?>> deserializer();
    Class<?> forType();
}
