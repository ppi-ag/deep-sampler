/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonSerializer;

import java.lang.annotation.*;

/**
 * This annotation can be used to register a {@link JsonSerializer} for the underlying Jackson JSON parser.
 * <p>
 * The annotation can be used on
 * <ul>
 *     <li>test methods</li>
 *     <li>test classes</li>
 *     <li>{@link SamplerFixture}-classes</li>
 *     <li>the method {@link SamplerFixture#defineSamplers()}</li>
 * </ul>
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
    Class<?> forType();
}
