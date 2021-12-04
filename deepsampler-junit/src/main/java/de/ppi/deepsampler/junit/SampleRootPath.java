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
 * Relative paths to sample files are resolved against ./ by default. {@link SampleRootPath} can be used to define
 * another root path for relative paths.
 * <p>
 * {@link SampleRootPath} is used for sample files that are saved by @{@link SaveSamples} and loaded by {@link LoadSamples}.
 * {@link LoadSamples} uses the root path only, if the sample file is loaded from file system, not from classpath.
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
