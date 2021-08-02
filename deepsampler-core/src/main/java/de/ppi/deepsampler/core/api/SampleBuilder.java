package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.SampleDefinition;

import java.util.Objects;

/**
 * Parent off all {@link SampleBuilder}s. A {@link SampleBuilder} is a Builder that is used to compose a {@link SampleDefinition} using a
 * fluent API in conjunction with {@link Sample} and {@link PersistentSample}.
 *
 * DeepSampler defines different {@link SampleDefinition}s depending on the type of method that should be stubbed. For instance, different
 * configurations are necessary for void methods and methods that return values.
 */
public abstract class SampleBuilder {

    private final SampleDefinition sampleDefinition;

    /**
     * Create a {@link SampleBuilder} with a {@link SampleDefinition}
     *
     * @param sampleDefinition {@link SampleDefinition}
     */
    protected SampleBuilder(final SampleDefinition sampleDefinition) {
        this.sampleDefinition = Objects.requireNonNull(sampleDefinition, "the SampleDefinition must not be null.");
    }

    protected SampleDefinition getSampleDefinition() {
        return sampleDefinition;
    }

}
