package org.deepsampler.core.internal.aophandler;

import org.deepsampler.core.api.Quantity;
import org.deepsampler.core.error.VerifyException;
import org.deepsampler.core.model.*;

import java.lang.reflect.Method;

public class VerifySampleHandler extends ReturningSampleHandler {
    private final Quantity quantity;
    private final Class<?> cls;

    public VerifySampleHandler(final Quantity quantity, final Class<?> cls) {
        this.quantity = quantity;
        this.cls = cls;
    }


    @Override
    public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) {
        final SampledMethod sampledMethod = new SampledMethod(cls, thisMethod);
        final SampleDefinition sampleDefinition = SampleRepository.getInstance().find(sampledMethod, args);

        if (sampleDefinition != null) {
            final ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(cls);
            final SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(sampleDefinition);

            final int expected = quantity.getTimes();
            final int actual = sampleExecutionInformation.getTimesInvoked();

            if (expected != actual) {
                throw new VerifyException(sampleDefinition.getSampledMethod(), expected, actual);
            }
        } else if (quantity.getTimes() != 0) {
            throw new VerifyException(sampledMethod, args, quantity.getTimes(), 0);
        }
        return createEmptyProxy(thisMethod.getReturnType());
    }
}
