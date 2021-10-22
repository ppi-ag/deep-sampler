/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.Answer;
import de.ppi.deepsampler.core.model.SampleDefinition;

/**
 * <p>
 * Provides a fluent API for creating a {@link SampleDefinition} for methods that return a value (i.e. functions).
 * </p>
 *
 * <p>
 * With the FunctionalSampleBuilder you are able to define:
 *   <ul>
 *       <li>A concrete return value</li>
 *       <li>An abstract {@link Answer} that executes arbitrary code and returns a value</li>
 *   </ul>
 * <p>
 *   The return value will be used (and evaluated) when the stubbed method will be invoked.
 * </p>
 *
 * @param <T> type of the class you want stub
 */
public class FunctionalSampleBuilder<T> extends VoidSampleBuilder {

    /**
     * Create a {@link FunctionalSampleBuilder} with a sampler of the class you want to build a sample for, and the sampleDefinition
     * you want to extend.
     *
     * @param sampler          the sampler {@link Sampler}
     * @param sampleDefinition {@link SampleDefinition}
     */
    @SuppressWarnings("unused")
    public FunctionalSampleBuilder(final T sampler, final SampleDefinition sampleDefinition) {
        super(sampleDefinition);
    }

    /**
     * Defines the value, that will be returned when the stubbed method is invoked. This value is called the "Sample".
     *
     * @param sampleReturnValue the return value that should be returned by the stubbed method.
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
     * @param answer supplier you want to get evaluated when the stubbed method got invoked
     */
    @SuppressWarnings("unchecked")
    public void answers(final Answer<? extends Throwable> answer) {
        getSampleDefinition().setAnswer((Answer<Throwable>) answer);
    }

}
