/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.charset.Charset;

/**
 * Defines the charset, that is used while reading and writing samples.
 *
 * If no charset is set explicitly, {@link Charset#defaultCharset()} is used, to define the default.
 *
 * This annotation can be used on test methods, test classes, {@link SamplerFixture}s and
 * {@link SamplerFixture#defineSamplers()}
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseCharset {

    /**
     * The name of the desired charset, as defined by {@link java.nio.charset.Charset#forName(String)}.
     * @return The name of the desired charset, as defined by {@link java.nio.charset.Charset#forName(String)}.
     */
    String value();
}
