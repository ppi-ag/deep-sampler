/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A container-annotation for {@link UseJsonDeserializer} that allows {@link UseJsonDeserializer} to be repeatable.
 * Use the annotation {@link UseJsonDeserializer}, if you want to add a custom {@link com.fasterxml.jackson.databind.JsonDeserializer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseJsonDeserializers {
    UseJsonDeserializer[] value();
}
