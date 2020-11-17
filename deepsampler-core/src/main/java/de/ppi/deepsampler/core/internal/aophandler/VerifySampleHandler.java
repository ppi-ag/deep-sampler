package de.ppi.deepsampler.core.internal.aophandler;

import de.ppi.deepsampler.core.api.Quantity;
import de.ppi.deepsampler.core.error.VerifyException;
import de.ppi.deepsampler.core.model.*;

import java.lang.reflect.Method;
import java.util.List;

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

            final List<SampleDefinition> similarDefinition = SampleRepository.getInstance().findAllForMethod(sampledMethod);
            if (!similarDefinition.isEmpty()) {
                final ExecutionInformation executionInformation = ExecutionRepository.getInstance().getOrCreate(cls);
                for (final SampleDefinition similarSampleDefinition : similarDefinition) {
                    final SampleExecutionInformation sampleExecutionInformation = executionInformation.getOrCreateBySample(similarSampleDefinition);
                    if (sampleExecutionInformation.getTimesInvoked() != 0) {
                        throw new VerifyException(similarSampleDefinition, args, sampleExecutionInformation.getTimesInvoked());
                    }
                }
            }
        }


        if (sampleDefinition == null && quantity.getTimes() != 0) {
            throw new VerifyException(sampledMethod, args, quantity.getTimes(), 0);
        }
        return createEmptyProxy(thisMethod.getReturnType());
    }
}
