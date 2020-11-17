package de.ppi.deepsampler.core.api;

import de.ppi.deepsampler.core.internal.ProxyFactory;
import de.ppi.deepsampler.core.internal.aophandler.RecordSampleHandler;
import de.ppi.deepsampler.core.model.ExecutionRepository;
import de.ppi.deepsampler.core.model.SampleRepository;

/**
 * <p>
 * A sampler in DeepSampler is an entity which is able to define samples. To define this samples the sampler
 * will create a proxy object of the given class you want to define samples for. As a followup you have
 * to use the API provided by {@link Sample} to finish the definition of your sample.
 * </p>
 *
 * Example: <br><br>
 * <p>
 *     <code>
 *         // Create a sampler<br>
 *         TestService testServiceSampler = Sampler.prepare(TestService.class); <br>
 *         <br>
 *         // With this sampler we are able to select the method we want to stub.<br>
 *         Sample.of(testServiceSampler.myTestMethod("ABC")).is("Hi");
 *     </code>
 * </p>
 */
public class Sampler {

    private Sampler() {
        //  The constructor is private since this utility class is not intended to be instantiated.
    }

    /**
     * Clear all states of DeepSampler
     */
    public static void clear() {
        SampleRepository.getInstance().clear();
        ExecutionRepository.getInstance().clear();
    }

    /**
     * Create a sampler for the given class.
     *
     * @param cls the cls you want to define a sampler for
     * @param <T> the type of cls
     * @return the sampler (proxy instance of T)
     */
    public static <T> T prepare(final Class<T> cls) {
        return ProxyFactory.createProxy(cls, new RecordSampleHandler(cls));
    }

}
