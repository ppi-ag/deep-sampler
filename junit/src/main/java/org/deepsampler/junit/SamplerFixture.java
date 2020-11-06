package org.deepsampler.junit;

/**
 * {@link SamplerFixture}s are a convenient way to share a set of Samplers with multiple test cases. A {@link SamplerFixture} can also bee seen
 * as a definition of a test-component, since Samplers define the boundary of a component.
 *
 * The Fixture can be used in a JUnit-Test by annotating a method or a test class with {@link UseSamplerFixture}
 *
 * Properties that are annotated with {@link PrepareSampler} are automatically populated with a sampled instance of the property
 * created by {@link org.deepsampler.core.api.Sampler#prepare(Class)}.
 *
 * The concrete implementation must have a default constructor without any parameters.
 */
public interface SamplerFixture {

    /**
     * The Samplers of a test-component, or an arbitrary set of Samplers can be defined in this method.
     */
    void defineSamplers();
}
