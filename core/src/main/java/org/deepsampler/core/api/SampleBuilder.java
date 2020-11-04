package org.deepsampler.core.api;

import org.deepsampler.core.model.ReturnValueSupplier;
import org.deepsampler.core.model.SampleDefinition;

import java.util.Objects;

/**
 * <p>
 *  Provides a fluent API for creating a sampleDefinition. You should never create a {@link SampleBuilder} by
 *  yourself instead you should use {@link Sample#of(Object)}.
 * </p>
 *
 * <p>
 *   With the sampleBuilder you are able to define:
 *   <ul>
 *       <li>A return value or returnValueSupplier</li>
 *       <li>A sampleId</li>
 *   </ul>
 *
 *   The return value will be used (and evaluated) when the stubbed method will be invoked.
 *   The sampleId is a possibility to assign an id to the sampleDefinition. This id will be used
 *   in the persistence to identify the stubbed method.
 * </p>
 *
 * @param <T> type of the class you want stub
 */
public class SampleBuilder<T> {

    private final SampleDefinition sampleDefinition;

    /**
     * Create a {@link SampleBuilder} with a sampler of the class you want to build a sample for, and the sampleDefinition
     * you want to extend.
     *
     * @param sampler the sampler {@link Sampler}
     * @param sampleDefinition {@link SampleDefinition}
     */
    @SuppressWarnings("unused")
    public SampleBuilder(final T sampler, final SampleDefinition sampleDefinition) {
        Objects.requireNonNull(sampleDefinition, "the SampleDefinition must not be null.");

        this.sampleDefinition = sampleDefinition;
    }

    /**
     * Makes the stubbed method return the given value when invoked.
     *
     * @param sampleReturnValue the return value you want to set for the sampleDefinition
     */
    public void is(final T sampleReturnValue) {
        sampleDefinition.setReturnValueSupplier(() -> sampleReturnValue);
    }

    /**
     * Makes the stubbed method return the given value evaluated by the {@link ReturnValueSupplier} when invoked.
     *
     * @param propertySupplier supplier you want to get evaluated when the stubbed method get invoked
     */
    public void is(final ReturnValueSupplier propertySupplier) {
        sampleDefinition.setReturnValueSupplier(propertySupplier);
    }

    /**
     * Set an id for the current SampleDefinition.
     *
     * @param sampleId the sampleId you want to set
     * @return this
     */
    public SampleBuilder<T> hasId(final String sampleId) {
        sampleDefinition.setSampleId(sampleId);
        return this;
    }

}
