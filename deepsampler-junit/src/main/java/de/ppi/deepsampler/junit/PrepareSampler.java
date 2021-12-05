/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit;

import de.ppi.deepsampler.core.api.Sampler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Properties that are annotated with {@link PrepareSampler} are automatically populated with a sampled instance of the property
 * created by {@link Sampler#prepare(Class)}.
 *
 * This Annotation is used by the DeepSamplerRule (junit4) and the DeepSamplerExtension (junit5).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrepareSampler {

}
