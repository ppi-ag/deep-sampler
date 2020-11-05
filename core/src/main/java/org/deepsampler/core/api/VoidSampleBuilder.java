package org.deepsampler.core.api;

import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.VoidAnswer;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.util.Objects;

/**
 * <p>
 * Provides a fluent API for creating a {@link SampleDefinition}. You should never create a {@link SampleBuilder} by
 * yourself instead you should use {@link Sample#of(Object)}.
 * </p>
 *
 * <p>
 * With the {@link VoidSampleBuilder} you are able to:
 *   <ul>
 *       <li>throw Exceptions when a stubbed method is called</li>
 *       <li>deactivate a method, so that a stubbed method does nothing when it is called</li>
 *   </ul>
 */
public class VoidSampleBuilder {

    private final SampleDefinition sampleDefinition;

    /**
     * Create a {@link VoidSampleBuilder} with a {@link SampleDefinition}
     * you want to extend.
     *
     * @param sampleDefinition {@link SampleDefinition}
     */
    public VoidSampleBuilder(final SampleDefinition sampleDefinition) {
        Objects.requireNonNull(sampleDefinition, "the SampleDefinition must not be null.");

        this.sampleDefinition = sampleDefinition;
    }

    protected SampleDefinition getSampleDefinition() {
        return sampleDefinition;
    }

    /**
     * Makes the stubbed method throw an {@link Exception} when it is called.
     *
     * @param exception the Exception that will be thrown.
     */
    public void throwsException(final Exception exception) {
        sampleDefinition.setAnswer(invocation -> {
            throw exception;
        });
    }

    /**
     * Makes the stubbed method throw an {@link Exception} when it is called.
     *
     * @param exceptionClass The type of the {@link Exception} that will be thrown. The Exception is instantiated at the time when the stubbed method is called.
     */
    public void throwsException(final Class<? extends Exception> exceptionClass) {
        sampleDefinition.setAnswer(invocation -> {
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
     * don't exist in the real database. In another case it might simply be necessary to prevent a method from deleting data.
     */
    public void doesNothing() {
        sampleDefinition.setAnswer(invocation -> null);
    }

    /**
     * Sometimes it is necessary to execute some logic that would replace the original logic of a stubbed method.
     * This can be done by using an Answer like so:
     *
     * <code>
     * Sample.of(() -> sampler.doSomething())).answer(invocation -> doSomethingElse());
     * </code>
     *
     * In essence using Answers gives free control on what a stubbed method should do.
     *
     * @param answer method you want to get evaluated when the stubbed method is invoked
     */
    public void answers(final VoidAnswer<?> answer) {
        sampleDefinition.setAnswer(stubMethodInvocation -> {
            answer.call(stubMethodInvocation);
            return null;
        });
    }
}
