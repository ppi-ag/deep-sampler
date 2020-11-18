/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.core.internal.api.ExecutionManager;
import de.ppi.deepsampler.core.model.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;

/**
 * A SamplerInterceptor for SpringApplications.
 */
@Aspect
public class SpringSamplerInterceptor {


    /**
     * Intercepts all Methods in SpringBeans and delegates to {@link SampleDefinition}s that have been defined within test classes.
     *
     * @param joinPoint The AOP-JoinPoint that describes which method will be called and which method can now be intercepted. I.e. this method
     *                  will become a stub if the method is described by a Sampler.
     * @return If the intercepted method is a stub, the return value is determined by the Sampler, otherwise this is the original return value comming from
     * the original method as described by the joinPoint.
     * @throws Throwable Since we intercept all methods in general it is possible that any kind of {@link Exception}, even {@link Throwable} is thrown.
     * Even though declaring {@link Throwable} in a throws clause is usually not recommended, this is done by Spring itself (for comprehensible reasons),
     * so we are also forced to do so.
     */
    @SuppressWarnings("unused")
    @Around("execution(* *(..)) && !target(DeepSamplerSpringConfig)")
    public Object aroundMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
        final SampleDefinition sampleDefinition = findSampleDefinition(joinPoint);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            final Answer<?> answer = sampleDefinition.getAnswer();

            if (answer != null) {
                final StubMethodInvocation stubMethodInvocation = new StubMethodInvocation(Arrays.asList(joinPoint.getArgs()), joinPoint.getThis());
                return sampleDefinition.getAnswer().call(stubMethodInvocation);
            } else {
                // no returnValueSupplier -> we have to log the invocations for recordings
                final Object returnValue = joinPoint.proceed();
                ExecutionManager.record(sampleDefinition, new MethodCall(returnValue, Arrays.asList(joinPoint.getArgs())));
                return returnValue;
            }
        }

        return joinPoint.proceed();
    }

    /**
     * Searches for a {@link SampleDefinition} that matches to a particular method call.
     * @param proceedingJoinPoint describes the intercepted method. If a {@link SampleDefinition} for this method hs beed defined, this method will be stubbed.
     * @return If the intercepted method (as described by proceedingJoinPoint) has a Sampler the {@link SampleDefinition} will be returned, otherwise null.
     */
    private SampleDefinition findSampleDefinition(final ProceedingJoinPoint proceedingJoinPoint) {
        final MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        final Object interceptedObject = proceedingJoinPoint.getThis();

        final SampledMethod sampledMethod = new SampledMethod(interceptedObject.getClass(), signature.getMethod());

        return SampleRepository.getInstance().find(sampledMethod, proceedingJoinPoint.getArgs());
    }


}
