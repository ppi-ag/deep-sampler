/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * A container-annotation for {@link UseJsonSerializer} that allows {@link UseJsonSerializer} to be repeatable.
 * Use the annotation {@link UseJsonSerializer} if you want to add a custom {@link com.fasterxml.jackson.databind.JsonSerializer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseJsonSerializers {
    UseJsonSerializer[] value();
}
