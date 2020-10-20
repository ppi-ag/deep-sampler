package org.deepsampler.provider.guice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.*;

import java.util.Arrays;

public class GuiceSamplerInterceptor implements MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        SampleDefinition sampleDefinition = findSampleDefinition(invocation);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            ReturnValueSupplier returnValueSupplier = sampleDefinition.getReturnValueSupplier();

            if (returnValueSupplier != null) {
                return sampleDefinition.getReturnValueSupplier().supply();
            } else {
                // no returnValueSupplier -> we have to log the invocations for recordings
                Object returnValue = invocation.proceed();
                ExecutionManager.record(sampleDefinition, new MethodCall(Arrays.asList(invocation.getArguments()),
                        returnValue));
                return returnValue;
            }
        }

        return invocation.proceed();

    }

    private SampleDefinition findSampleDefinition(MethodInvocation invocation) {
        SampledMethod sampledMethod = new SampledMethod(invocation.getThis().getClass(), invocation.getMethod());
        return SampleRepository.getInstance().find(sampledMethod, invocation.getArguments());
    }
}
