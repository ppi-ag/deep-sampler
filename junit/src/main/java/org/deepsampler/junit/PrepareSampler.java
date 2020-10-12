package org.deepsampler.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Properties that are annotated with {@link PrepareSampler} are automatically populated with a sampled instance of the property
 * created by {@link org.deepsampler.core.api.Sampler#prepare(Class)}.
 * This Annotation is used by the DeepSamplerRule (junit4) and the DeepSamplerExtension (junit5).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrepareSampler {

}
