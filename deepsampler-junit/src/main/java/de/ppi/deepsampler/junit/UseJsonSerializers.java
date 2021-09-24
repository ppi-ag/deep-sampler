package de.ppi.deepsampler.junit;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseJsonSerializers {
    UseJsonSerializer[] value();
}
