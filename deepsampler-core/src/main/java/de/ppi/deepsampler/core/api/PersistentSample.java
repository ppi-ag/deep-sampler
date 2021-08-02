/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.error.NotASamplerException;
import de.ppi.deepsampler.core.internal.ProxyFactory;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;

import java.util.Objects;

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
public class PersistentSample {

    private PersistentSample() {
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
    public static <T> PersistentSampleBuilder<T> of(final T sampledMethodCall) {
        SampleDefinition currentSampleDefinition = SampleRepository.getInstance().getCurrentSampleDefinition();
        SampleDefinition lastSampleDefinition = SampleRepository.getInstance().getLastSampleDefinition();

        if (currentSampleDefinition == lastSampleDefinition) {
            throw new NotASamplerException("sampledMethodCall is not a Sampler. Did you prepare the Sampler using Sampler.prepare() or @PrepareSampler?");
        }

        currentSampleDefinition.setMarkedForPersistent(true);

        SampleRepository.getInstance().setLastSampleDefinition(currentSampleDefinition);
        return new PersistentSampleBuilder<>(sampledMethodCall, currentSampleDefinition);
    }

    /**
     * Along with the subsequent method call it defines a Sample for which the framework should start to track
     * how often this Sample is used in the component. This is necessary to be able to verify the invocation
     * of a specific method.
     *
     * @param sampler the sampler for which you want to activate a method call
     * @param <T> the type of the target Class/sampler
     * @return the sampler itself
     */
    public static <T> T forVerification(final T sampler) {
        Objects.requireNonNull(sampler);

        if (!ProxyFactory.isProxyClass(sampler.getClass())) {
            throw new NotASamplerException(sampler.getClass());
        }

        SampleRepository.getInstance().setMarkNextVoidSamplerForPersistence(true);

        return sampler;
    }



    /**
     * This method will set the <code>sampleId</code> of the last defined sampleDefinition. Mostly you
     * want to set the sampleId with the Method {@link PersistentSampleBuilder#hasId(String)}. But in case of
     * void-returning methods, it is not possible to create a {@link FunctionalSampleBuilder}. As a consequence
     * you will need to set the id with this method.
     *
     * @param id the id you want to set.
     */
    public static void setIdToLastMethodCall(final String id) {
        SampleRepository.getInstance().getCurrentSampleDefinition().setSampleId(id);
    }

}
