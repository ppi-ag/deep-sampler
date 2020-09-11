package org.deepsampler.core.api;

import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.internal.handler.VerifyBehaviorHandler;
import org.deepsampler.core.model.SampleRepository;

/**
 * This is the starting point for the definition of Samples in test classes.
 *
 * A "Sample" is is an exemplary return value or or an exemplary behavior of a method that fulfills a prerequisite of a particular test case. When the tested methods are not able
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


    /**
     * Defines a sampled method by calling the method inside of the parameter. The returned {@link SampleBuilder} will then offer possibilities to define the Sample,
     * or in other words, it offers possibilities to override the default behavior or the return value of a method.
     *
     * @param sampledMethodCall The method call that will be sampled.
     * @param <T>
     * @return A {@link SampleBuilder} which can be used to define the concrete Sample. <b>Do not</b> keep references to this object, it is intended to be used as a
     * fluent API only.
     */
    public static <T> SampleBuilder<T> of(T sampledMethodCall) {
        return new SampleBuilder<>(sampledMethodCall,
                SampleRepository.getInstance().getCurrentSampleDefinition());
    }

    public static <T> T verifyTrait(Class<T> cls, Quantity quantity) {
        return ProxyFactory.createProxy(cls, new VerifyBehaviorHandler(quantity, cls));
    }

}
