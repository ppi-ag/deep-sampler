package org.deepmock.provider.standalone;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.deepmock.core.model.SampleDefinition;
import org.deepmock.core.model.SampleRepository;
import org.deepmock.core.model.SampledMethod;
import org.deepmock.provider.common.SamplerInterceptor;
import org.deepmock.core.internal.api.ExecutionManager;

public class GuiceSamplerInterceptor implements SamplerInterceptor, MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        SampleDefinition sampleDefinition = findSampleDefinition(invocation);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            return sampleDefinition.getReturnValueSupplier().supply();
        } else {
            return invocation.proceed();
        }
    }

    private SampleDefinition findSampleDefinition(MethodInvocation invocation) {
        SampledMethod sampledMethod = new SampledMethod(invocation.getThis().getClass(), invocation.getMethod());
        return SampleRepository.getInstance().find(sampledMethod, invocation.getArguments());
    }
}
