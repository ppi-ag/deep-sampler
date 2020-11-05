package org.deepsampler.provider.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.*;

import java.util.Arrays;

/**
 * A SamplerInterceptor for SpringApplications.
 */
@Aspect
public class SpringSamplerInterceptor {


    /**
     * Intercepts all Methods in SpringBeans and delegates to {@link SampleDefinition}s that have been defined within test classes.
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @SuppressWarnings("unused")
    @Around("execution(* *(..)) && !target(DeepSamplerSpringConfig)")
    public Object aroundMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
        final SampleDefinition sampleDefinition = findSampleDefinition(joinPoint);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            final Answer answer = sampleDefinition.getAnswer();

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
     * @param proceedingJoinPoint
     * @return
     */
    private SampleDefinition findSampleDefinition(final ProceedingJoinPoint proceedingJoinPoint) {
        final MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        final Object interceptedObject = proceedingJoinPoint.getThis();

        final SampledMethod sampledMethod = new SampledMethod(interceptedObject.getClass(), signature.getMethod());

        return SampleRepository.getInstance().find(sampledMethod, proceedingJoinPoint.getArgs());
    }


}
