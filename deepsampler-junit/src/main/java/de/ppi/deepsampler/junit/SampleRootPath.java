/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Persistence extensions may use this annotation to define the root path, under which samples are saved.
 * <p>
 * This annotation may be used on the test class and on the {@link SamplerFixture}. If both are annotated, the one on
 * the test class overrides the one on the {@link SamplerFixture}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SampleRootPath {

    /**
     * Defines the root path for relative paths.
     *
     * @return The root path under which sample files are stored.
     */
    String value();
}
