package org.deepsampler.provider.standalone;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.core.model.SampledMethod;
import org.deepsampler.provider.common.SamplerInterceptor;
import org.deepsampler.core.internal.api.ExecutionManager;

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
