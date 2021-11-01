/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import com.fasterxml.jackson.databind.JsonDeserializer;

import java.lang.annotation.*;

/**
 * This annotation can be used to register a {@link JsonDeserializer} for the underlying Jackson JSON parser.
 *
 * The annotation can be placed on
 * <ul>
 *     <li>a test method</li>
 *     <li>the class that declares the test method</li>
 *     <li>the method {@link SamplerFixture#defineSamplers()}</li>
 *     <li>the class that defines the method defineSamplers</li>
 * </ul>
 * *
 * If {@link UseJsonDeserializer} is used at more than one of the aforementioned places, and several of them define {@link JsonDeserializer}s
 * for the same type, the ones on top of the aforementioned list, override the lower ones.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Repeatable(UseJsonDeserializers.class)
public @interface UseJsonDeserializer {

    /**
     * Defines the class of the concrete {@link com.fasterxml.jackson.databind.JsonDeserializer}
     *
     * @return The class of the concrete {@link com.fasterxml.jackson.databind.JsonDeserializer}
     */
    @SuppressWarnings("java:S1452") // The generic wildcard is necessary because we want to allow all kinds of JsonSerializers here.
            Class<? extends JsonDeserializer<?>> deserializer();

    /**
     * The {@link Class} for which the deserializer should be used.
     *
     * @return The {@link Class} for which the deserializer should be used.
     */
    Class<?> forType();
}