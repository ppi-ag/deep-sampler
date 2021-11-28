/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.NotASamplerException;
import de.ppi.deepsampler.core.error.VerifyException;
import de.ppi.deepsampler.core.internal.ProxyFactory;
import de.ppi.deepsampler.core.internal.aophandler.VerifySampleHandler;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;

/**
 * This is the starting point for the definition of Samples in test classes.
 *
 * A "Sample" is an exemplary return value or an exemplary behavior of a method that fulfills a prerequisite of a particular test case. When the tested methods are not able
 * to reproduce the Sample by their own means (i.e. due to changes in the underlying database, or passing of time, etc.), the methods can be coerced to reproduce the Sample using
 * this API.
 *
 * A method is called "sampled" if the method is coerced to return a Sample.
 *
 * Objects which contain sampled methods, are called "Sampler".
 *
 * DeepSampler defines Samples on classes rather then on objects. This enables DeepSampler to change the behavior of all instances of these classes without the need to instantiate
 * the Sampler manually and to distribute the Sampler into the objects that will be tested. The distribution is done by a Dependency Injection Framework like Spring or Guice.
 *
 * Methods of the class {@link Object} are ignored. Otherwise strange effects might appear, e.g. if Object::finalize is
 * called by the garbage collector.
 *
 * @author Jan Schankin, Rico Schrage
 */
public class Sample {

    private Sample() {
        // This class is meant to be used as a frontend for a static fluent API and should never be instantiated.
    }


    /**
     * Defines a sampled method by calling the method inside of the parameter. The returned {@link FunctionalSampleBuilder} will then offer possibilities to define the Sample,
     * or in other words, it offers possibilities to override the default behavior or the return value of a method.
     *
     * @param sampledMethodCall The method call that will be sampled.
     * @param <T> The type of the return value and therefore the type of the Sample.
     * @return A {@link FunctionalSampleBuilder} which can be used to define the concrete Sample. <b>Do not</b> keep references to this object, it is intended to be used as a
     * fluent API only.
     */
    public static <T> FunctionalSampleBuilder<T> of(final T sampledMethodCall) {
        SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        SampleDefinition lastSampleDefinition = SampleRepository.getInstance().getLastSampleDefinition();

        if (currentSampleDefinition == lastSampleDefinition) {
            throw new NotASamplerException("sampledMethodCall is not a Sampler. Did you prepare the Sampler using Sampler.prepare() or @PrepareSampler?");
        }

        SampleRepository.getInstance().setLastSampleDefinition(currentSampleDefinition);

        return new FunctionalSampleBuilder<>(sampledMethodCall, currentSampleDefinition);
    }


    /**
     * Along with the subsequent method call you can assert that this method call has been called
     * <code>quantity</code>-times. If the quantity doesn't match exactly this method
     * will throw a {@link VerifyException}. If it does match nothing will happen.
     *
     * @param cls the class of which you want to assert a specific method invocation
     * @param quantity the quantity you expect this invocation to happen
     * @param <T> the type of the class you want assert
     * @return Proxy to subsequently define the method which invocation you want to assert
     */
    public static <T> T verifyCallQuantity(final Class<T> cls, final Quantity quantity) {
        return ProxyFactory.createProxy(cls, new VerifySampleHandler(quantity, cls));
    }

    /**
     * Defines a stubbed void method by calling the method inside of a lambda. The returned {@link VoidSampleBuilder} will then offer possibilities to define the Sample,
     * or in other words, it offers possibilities to override the default behavior or the stubbed method.
     *
     * @param sampledMethodCall The method call as a lambda expression, that will be sampled.
     * @param <E> If the stubbed method (called by the functional interface {@link VoidCall}) throws an {@link Exception}, E defines the type of that {@link Exception}
     *           E may be a caught or an uncaught (i.e. {@link RuntimeException} Exception.
     * @return A {@link VoidSampleBuilder} which can be used to define the concrete Sample. <b>Do not</b> keep references to this object, it is intended to be used as a
     * fluent API only.
     */
    public static <E extends Exception> VoidSampleBuilder of(final VoidCall<E> sampledMethodCall) {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        final SampleDefinition lastSampleDefinition = sampleRepository.getCurrentSampleDefinition();

        try {
            sampledMethodCall.call();
        } catch (final Exception e) {
            throw new NotASamplerException("The VoidCall did throw an Exception. Did you call an unstubbed method inside of the lambda, " +
                    "instead of a method on a Sampler?", e);
        }

        final SampleDefinition newSampleDefinition = sampleRepository.getCurrentSampleDefinition();

        if (lastSampleDefinition == newSampleDefinition) {
            throw new NotASamplerException("sampledMethodCall did not call a method on a Sampler. Did you use a " +
                    "sampled object created by @PrepareSampler or Sampler.prepare()?");
        }

        return new VoidSampleBuilder(newSampleDefinition);
    }

}
