package de.ppi.deepsampler.provider.guice;

import de.ppi.deepsampler.core.internal.api.ExecutionManager;
import de.ppi.deepsampler.core.model.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Arrays;
import java.util.List;

public class GuiceSamplerInterceptor implements MethodInterceptor {


    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final SampleDefinition sampleDefinition = findSampleDefinition(invocation);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            final Answer<?> answer = sampleDefinition.getAnswer();

            if (answer != null) {
                final StubMethodInvocation stubMethodInvocation = new StubMethodInvocation(Arrays.asList(invocation.getArguments()), invocation.getThis());
                return sampleDefinition.getAnswer().call(stubMethodInvocation);
            } else {
                // no returnValueSupplier -> we have to log the invocations for recordings
                final Object returnValue = invocation.proceed();
                final List<Object> arguments = Arrays.asList(invocation.getArguments());

                ExecutionManager.record(sampleDefinition, new MethodCall(returnValue, arguments));
                return returnValue;
            }
        }

        return invocation.proceed();

    }

    private SampleDefinition findSampleDefinition(final MethodInvocation invocation) {
        final SampledMethod sampledMethod = new SampledMethod(invocation.getThis().getClass(), invocation.getMethod());
        return SampleRepository.getInstance().find(sampledMethod, invocation.getArguments());
    }
}
