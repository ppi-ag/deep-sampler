package org.deepsampler.core.api;

import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.internal.aophandler.RecordSampleHandler;
import org.deepsampler.core.model.ExecutionRepository;
import org.deepsampler.core.model.SampleRepository;

public class Sampler {

    private Sampler() {
        //  The constructor is private since this utility class is not intended to be instantiated.
    }

    public static void clear() {
        SampleRepository.getInstance().clear();
        ExecutionRepository.getInstance().clear();
    }

    public static <T> T prepare(final Class<T> cls) {
        return ProxyFactory.createProxy(cls, new RecordSampleHandler(cls));
    }

}
