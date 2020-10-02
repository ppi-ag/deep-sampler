package org.deepsampler.core.internal.aophandler;

import org.deepsampler.core.api.Quantity;
import org.deepsampler.core.error.VerifyException;
import org.deepsampler.core.model.*;

import java.lang.reflect.Method;

public class VerifySampleHandler extends ReturningSampleHandler {
    private final Quantity quantity;
    private final Class<?> cls;

    public VerifySampleHandler(Quantity quantity, Class<?> cls) {
        this.quantity = quantity;
        this.cls = cls;
    }


    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) {
        SampledMethod sampledMethod = new SampledMethod(cls, thisMethod);
        SampleDefinition sampleDefinition = SampleRepository.getInstance().find(sampledMethod, args);

        if (sampleDefinition != null) {
            ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(cls);
            SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);

            int expected = quantity.getTimes();
            int actual = sampleExecutionInformation.getTimesInvoked();

            if (expected != actual) {
                throw new VerifyException(sampleDefinition.getSampledMethod(), expected, actual);
            }
        } else if (quantity.getTimes() != 0) {
            throw new VerifyException(sampledMethod, quantity.getTimes(), 0);
        }
        return createEmptyProxy(thisMethod.getReturnType());
    }
}
