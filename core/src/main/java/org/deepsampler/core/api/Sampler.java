package org.deepsampler.core.api;

import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.internal.aophandler.RecordSampleHandler;
import org.deepsampler.core.model.SampleRepository;

public class Sampler {

    public static void clear() {
        SampleRepository.getInstance().clear();
    }

    @SuppressWarnings("unchecked")
    public static <T> T prepare(final Class<T> cls) {
        return ProxyFactory.createProxy(cls, new RecordSampleHandler(cls));
    }

}
