/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.Answer;
import de.ppi.deepsampler.core.model.SampleDefinition;

/**
 * <p>
 * Provides a fluent API for creating a {@link SampleDefinition}. You should never create a {@link SampleBuilder} by
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
public class SampleBuilder<T> extends VoidSampleBuilder {

    /**
     * Create a {@link SampleBuilder} with a sampler of the class you want to build a sample for, and the sampleDefinition
     * you want to extend.
     *
     * @param sampler          the sampler {@link Sampler}
     * @param sampleDefinition {@link SampleDefinition}
     */
    @SuppressWarnings("unused")
    public SampleBuilder(final T sampler, final SampleDefinition sampleDefinition) {
        super(sampleDefinition);
    }

    /**
     * Makes the stubbed method return the given value when invoked.
     *
     * @param sampleReturnValue the return value you want to set for the sampleDefinition
     */
    public void is(final T sampleReturnValue) {
        getSampleDefinition().setAnswer(stubInvocation -> sampleReturnValue);
    }

    /**
     * In most cases it will be sufficient to define a fixed Sample as a return value for a stubbed method, but sometimes it
     * is necessary to execute some logic that would compute the return value or that would even change some additional state.
     * This can be done by using an Answer like so:
     *
     * <code>
     * Sample.of(sampler.echo(anyString())).answer(invocation -&gt; invocation.getParameters().get(0));
     * </code>
     * <p>
     * In essence using Answers gives free control on what a stubbed method should do.
     *
     * @param answer supplier you want to get evaluated when the stubbed method get invoked
     */
    @SuppressWarnings("unchecked")
    public void answers(final Answer<? extends Throwable> answer) {
        getSampleDefinition().setAnswer((Answer<Throwable>) answer);
    }



    /**
     * Set an id for the current SampleDefinition.
     *
     * @param sampleId the sampleId you want to set
     * @return this
     */
    public SampleBuilder<T> hasId(final String sampleId) {
        getSampleDefinition().setSampleId(sampleId);
        return this;
    }


}
