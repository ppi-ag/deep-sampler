package org.deepsampler.core.internal.handler;

import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;

import java.lang.reflect.Method;

public class RecordBehaviorHandler extends ReturningBehaviorHandler {
    private final Class<?> cls;

    public RecordBehaviorHandler(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public Object invoke(Object self, Method method, Method proceed, Object[] args) {
        SampleDefinition behavior = createBehavior(cls, method, args);

        SampleRepository.getInstance().add(behavior);

        return createEmptyProxy(method.getReturnType());
    }


}
