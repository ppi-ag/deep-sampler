/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.StubMethodInvocation;
import de.ppi.deepsampler.core.model.VoidAnswer;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

/**
 * <p>
 * Provides a fluent API for creating a {@link SampleDefinition} for void methods.
 * </p>
 *
 * <p>
 * With the {@link VoidSampleBuilder} you are able to:
 *   <ul>
 *       <li>throw Exceptions when a stubbed method is called</li>
 *       <li>deactivate a method, so that a stubbed method does nothing when it is called</li>
 *       <li>delegate to the original method, so that a stubbed method does the same as the original method when it is called</li>
 *   </ul>
 */
public class VoidSampleBuilder extends SampleBuilder {


    /**
     * Create a {@link VoidSampleBuilder} with a {@link SampleDefinition}
     *
     * @param sampleDefinition {@link SampleDefinition}
     */
    protected VoidSampleBuilder(SampleDefinition sampleDefinition) {
        super(sampleDefinition);
    }

    /**
     * Makes the stubbed method throw an {@link Exception} when it is called.
     *
     * @param exception the Exception that will be thrown.
     */
    public void throwsException(final Exception exception) {
        getSampleDefinition().setAnswer(invocation -> {
            throw exception;
        });
    }

    /**
     * Makes the stubbed method throw an {@link Exception} when it is called.
     *
     * @param exceptionClass The type of the {@link Exception} that will be thrown. The Exception is instantiated at the time when the stubbed method is called.
     */
    public void throwsException(final Class<? extends Exception> exceptionClass) {
        getSampleDefinition().setAnswer(invocation -> {
            final Objenesis objenesis = new ObjenesisStd();
            final ObjectInstantiator<?> exceptionInstantiator = objenesis.getInstantiatorOf(exceptionClass);

            throw (Exception) exceptionInstantiator.newInstance();
        });
    }

    /**
     * Prevents the execution of the stubbed method.
     *
     * There are several situation where this might be useful. One example would be a
     * method that would attempt to write data in a database using values that do not comply to a foreign key, since the values are now Samples that
     * don't exist in the real database. In another case, it might simply be necessary to prevent a method from deleting data.
     */
    public void doesNothing() {
        getSampleDefinition().setAnswer(invocation -> null);
    }

    /**
     * Calls the original unstubbed method. This is useful in conjunction with other Samples that are only supposed to be used for particular
     * parameter values while all other calls of the method, with different parameter values, should still call the original method.
     */
    public void callsOriginalMethod() {
        getSampleDefinition().setAnswer(StubMethodInvocation::callOriginalMethod);
    }

    /**
     * Sometimes it is necessary to execute some logic that would replace the original logic of a stubbed method.
     * This can be done by using an Answer like so:
     *
     * <code>
     * Sample.of(() -&gt; sampler.doSomething())).answer(invocation -&gt; doSomethingElse());
     * </code>
     *
     * In essence using Answers gives free control on what a stubbed method should do.
     *
     * @param answer method you want to get evaluated when the stubbed method is invoked
     */
    public void answers(final VoidAnswer<?> answer) {
        getSampleDefinition().setAnswer(stubMethodInvocation -> {
            answer.call(stubMethodInvocation);
            return null;
        });
    }
}
