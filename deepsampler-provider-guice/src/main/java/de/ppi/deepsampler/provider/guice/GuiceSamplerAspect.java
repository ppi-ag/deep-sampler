/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.guice;

import de.ppi.deepsampler.core.internal.api.ExecutionManager;
import de.ppi.deepsampler.core.model.*;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.util.Arrays;
import java.util.List;

/**
 * The AOP-Aspect that implements the actual stubbing of methods. This Aspect is applied to classes and methods as defined by {@link DeepSamplerModule}.
 */
public class GuiceSamplerAspect implements MethodInterceptor {


    /**
     * This method is executed before every intercepted method. If the intercepted method is a method that should be stubbed according to
     * any matching {@link SampleDefinition}, the method will be replaced by a stub. If DeepSampler is used in recording mode the stub will
     * call the original method and record the parameters and return values of each individual call. Otherwise the stub will return the
     * Samples as defined by the {@link SampleDefinition}.
     *
     * @param invocation The intercepted method and its parameters.
     * @return the return value from the original method or a Sample.
     * @throws Throwable Every intercepted method might throw any kind of Throwable.
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final SampleDefinition sampleDefinition = findSampleDefinition(invocation);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            final Answer<?> answer = sampleDefinition.getAnswer();

            if (answer != null) {
                final StubMethodInvocation stubMethodInvocation = new StubMethodInvocation(Arrays.asList(invocation.getArguments()),
                        invocation.getThis(),
                        invocation::proceed);
                return ExecutionManager.execute(sampleDefinition, stubMethodInvocation);
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

    /**
     * Searches for a {@link SampleDefinition} that would match the intercepted method call as described by {@code invocation}.
     * {@link SampleDefinition}s are created either using the {@link de.ppi.deepsampler.core.api.Sample}-API or using a persistent Sampler-JSON.
     *
     * @param invocation The intercepted method and its parameters.
     * @return The {@link SampleDefinition} or {@code null} if none was found.
     */
    private SampleDefinition findSampleDefinition(final MethodInvocation invocation) {
        final SampledMethod sampledMethod = new SampledMethod(invocation.getThis().getClass(), invocation.getMethod());
        return SampleRepository.getInstance().findValidated(sampledMethod, invocation.getArguments());
    }
}
