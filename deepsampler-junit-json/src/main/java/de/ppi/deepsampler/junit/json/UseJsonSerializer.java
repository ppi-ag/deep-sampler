/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit.json;

import com.fasterxml.jackson.databind.JsonSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used to register a {@link JsonSerializer} for the underlying Jackson JSON parser.
 * <p>
 * The annotation can be placed on
 * <ul>
 *     <li>a test method</li>
 *     <li>the class that declares the test method</li>
 *     <li>the method {@link de.ppi.deepsampler.junit.SamplerFixture#defineSamplers()}</li>
 *     <li>the class that defines the method defineSamplers</li>
 * </ul>
 *
 * If {@link UseJsonSerializer} is used at more than one of the aforementioned places, and several of them define {@link JsonSerializer}s
 * for the same type, the ones on top of the aforementioned list, override the lower ones.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(UseJsonSerializers.class)
public @interface UseJsonSerializer {

    /**
     * Defines the concrete class of the {@link com.fasterxml.jackson.databind.JsonSerializer}
     *
     * @return the concrete class of the {@link com.fasterxml.jackson.databind.JsonSerializer}
     */
    @SuppressWarnings("java:S1452") // The generic wildcard is necessary because we want to allow all kinds of JsonSerializers here.
    Class<? extends JsonSerializer<?>> serializer();

    /**
     * The {@link Class} for which the serializer should be used.
     *
     * @return The {@link Class} for which the serializer should be used.
     */
    @SuppressWarnings("java:S1452") // The generic wildcard is necessary because we want to allow all kinds of JsonSerializers here.
    Class<?> forType();
}
