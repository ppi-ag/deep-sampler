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
 * {@link SamplerFixture}s are a convenient way to share a set of Samplers with multiple test cases. If a method within a Junit-Test,
 * or the test-class itself, is annotated with {@link UseSamplerFixture}, the associated {@link SamplerFixture} and all
 * Samplers that are defined by the {@link SamplerFixture} are prepared before the test method is executed.
 *
 * If a class is annotated with {@link UseSamplerFixture} the {@link SamplerFixture} is applied to all test-methods in that class.
 * Annotations on methods override the annotation on classes.
 *
 * This Annotation is used by the DeepSamplerRule (junit4) and the DeepSamplerExtension (junit5).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UseSamplerFixture {

    /**
     *
     * @return the class of a {@link SamplerFixture} that should be used for a test method.
     */
    Class<? extends SamplerFixture> value();
}
