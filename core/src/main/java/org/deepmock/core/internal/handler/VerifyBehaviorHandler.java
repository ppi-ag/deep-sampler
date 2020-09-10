package org.deepmock.core.internal.handler;

import org.deepmock.core.api.Quantity;
import org.deepmock.core.error.VerifyException;
import org.deepmock.core.model.*;

import java.lang.reflect.Method;

public class VerifyBehaviorHandler extends ReturningBehaviorHandler {
    private final Quantity quantity;
    private final Class<?> cls;

    public VerifyBehaviorHandler(Quantity quantity, Class<?> cls) {
        this.quantity = quantity;
        this.cls = cls;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) {
        SampledMethod sampledMethod = new SampledMethod(cls, thisMethod);
        SampleDefinition behavior = SampleRepository.getInstance().find(sampledMethod, args);

        if (behavior != null) {
            ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(cls);
            SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(behavior);

            int expected = quantity.getTimes();
            int actual = sampleExecutionInformation.getTimesInvoked();

            if (expected != actual) {
                throw new VerifyException(behavior.getSampledMethod(), expected, actual);
            }
        } else if (quantity.getTimes() != 0) {
            throw new VerifyException(sampledMethod, quantity.getTimes(), 0);
        }
        return createEmptyProxy(thisMethod.getReturnType());
    }
}
