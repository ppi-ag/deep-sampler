package org.deepsampler.core.api;

import org.deepsampler.core.internal.FixedQuantity;
import org.deepsampler.core.internal.ProxyFactory;
import org.deepsampler.core.internal.handler.RecordBehaviorHandler;
import org.deepsampler.core.model.SampleRepository;

public class Sampler {

    public static void clear() {
        SampleRepository.getInstance().clear();
    }

    @SuppressWarnings("unchecked")
    public static <T> T prepare(Class<T> cls) {
        return ProxyFactory.createProxy(cls, new RecordBehaviorHandler(cls));
    }

    public static Quantity times(int i) {
        return new FixedQuantity(i);
    }

}
