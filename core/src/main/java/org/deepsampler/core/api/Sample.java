package org.deepsampler.core.api;

import org.deepsampler.core.error.NotASamplerException;
import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.internal.aophandler.VerifySampleHandler;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;

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
 * @author Jan Schankin, Rico Schrage
 */
public class Sample {

    private Sample() {
        // This class is meant to be used as a frontend for a static fluent API and should never be instantiated.
    }


    /**
     * Defines a sampled method by calling the method inside of the parameter. The returned {@link SampleBuilder} will then offer possibilities to define the Sample,
     * or in other words, it offers possibilities to override the default behavior or the return value of a method.
     *
     * @param sampledMethodCall The method call that will be sampled.
     * @param <T> The type of the return value and therefore the type of the Sample.
     * @return A {@link SampleBuilder} which can be used to define the concrete Sample. <b>Do not</b> keep references to this object, it is intended to be used as a
     * fluent API only.
     */
    public static <T> SampleBuilder<T> of(final T sampledMethodCall) {
        return new SampleBuilder<>(sampledMethodCall,
                SampleRepository.getInstance().getCurrentSampleDefinition());
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

        return sampler;
    }

    /**
     * Along with the subsequent method call you can assert that this method call has been called
     * <code>quantity</code>-times. If the quantity doesn't match exactly this method
     * will throw a {@link org.deepsampler.core.error.VerifyException}. If it does match nothing will happen.
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
     * This method will set the <code>sampleId</code> of the last defined sampleDefinition. Mostly you
     * want to set the sampleId with the Method {@link SampleBuilder#hasId(String)}. But in case of
     * void-returning methods, it is not possible to create a {@link SampleBuilder}. As a consequence
     * you will need to set the id with this method.
     *
     * @param id the id you want to set.
     */
    public static void setIdToLastMethodCall(final String id) {
        SampleRepository.getInstance().getCurrentSampleDefinition().setSampleId(id);
    }

    /**
     * Defines a stubbed void method by calling the method inside of a lambda. The returned {@link VoidSampleBuilder} will then offer possibilities to define the Sample,
     * or in other words, it offers possibilities to override the default behavior or the stubbed method.
     *
     * @param sampledMethodCall The method call as a lambda expression, that will be sampled.
     * @return A {@link VoidSampleBuilder} which can be used to define the concrete Sample. <b>Do not</b> keep references to this object, it is intended to be used as a
     * fluent API only.
     */
    public static VoidSampleBuilder of(final VoidCall sampledMethodCall) {
        final SampleRepository sampleRepository = SampleRepository.getInstance();

        final SampleDefinition lastSampleDefinition = sampleRepository.getCurrentSampleDefinition();

        try {
            sampledMethodCall.call();
        } catch (final Exception e) {
            throw new NotASamplerException("The VoidCall did throw an Exception. Did you call an unstubbed method inside of the lamda, " +
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
