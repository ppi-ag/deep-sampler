/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.Answer;
import de.ppi.deepsampler.core.model.SampleDefinition;

/**
 * <p>
 * Provides a fluent API for creating a {@link SampleDefinition}. You should never create a {@link PersistentSampleBuilder} by
 * yourself instead you should use {@link Sample#of(Object)}.
 * </p>
 *
 * <p>
 * With the sampleBuilder you are able to define:
 *   <ul>
 *       <li>A return value or returnValueSupplier</li>
 *       <li>A sampleId</li>
 *   </ul>
 * <p>
 *   The return value will be used (and evaluated) when the stubbed method will be invoked.
 *   The sampleId is a possibility to assign an id to the sampleDefinition. This id will be used
 *   in the persistence to identify the stubbed method.
 * </p>
 *
 * @param <T> type of the class you want stub
 */
public class PersistentSampleBuilder<T> extends VoidSampleBuilder {

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
     * Set an id for the current SampleDefinition.
     *
     * @param sampleId the sampleId you want to set
     * @return this
     */
    public PersistentSampleBuilder<T> hasId(final String sampleId) {
        getSampleDefinition().setSampleId(sampleId);
        return this;
    }


}
