/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.SampleDefinition;

/**
 * <p>
 * Provides a fluent API for creating a {@link SampleDefinition} for persistent Samples that are loaded from a file or any other DataSource.
 * The persistence (i.e. recording and loading samples) is managed by PersistentSampleManager.
 * </p>
 *
 * <p>
 * With the sampleBuilder you are able to define a sampleId, that is used to identify a Sample in the persistence. By default
 * DeepSampler creates a sampleId from the signature of the stubbed method. If this signature is changed, maybe because of a future
 * refactoring, the sample cannot be loaded from the persistence anymore. Defining manual sampleIds can be used to avoid this situation.
 *
 * </p>
 *
 * @param <T> type of the class you want stub
 */
public class PersistentSampleBuilder<T> extends SampleBuilder {

    /**
     * Create a {@link PersistentSampleBuilder} with a sampler of the class you want to build a sample for, and the sampleDefinition
     * you want to extend.
     *
     * @param sampler          the sampler {@link Sampler}
     * @param sampleDefinition {@link SampleDefinition}
     */
    @SuppressWarnings("unused")
    public PersistentSampleBuilder(final T sampler, final SampleDefinition sampleDefinition) {
        super(sampleDefinition);
    }

    /**
     * Set an id for the current SampleDefinition. The Id is used to find a Sample for a stubbed method. By default
     * sampleIds are generated from the signature of the sampled method. Therefore a change of the signature would mean, that
     * DeepSample isn't able anymore to find the Sample for the method. To prevent this situation, manual sampleIds can be used.
     *
     * @param sampleId the sampleId you want to set
     * @return this
     */
    public PersistentSampleBuilder<T> hasId(final String sampleId) {
        getSampleDefinition().setSampleId(sampleId);
        return this;
    }


}
