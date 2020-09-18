package org.deepsampler.provider.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.core.model.SampledMethod;
import org.deepsampler.provider.common.SamplerInterceptor;

/**
 * A {@link SamplerInterceptor} for SpringApplications.
 */
@Aspect
public class SpringSamplerInterceptor implements SamplerInterceptor {


    /**
     * Intercepts all Methods in SpringBeans and delegates to {@link SampleDefinition}s that have been defined within test classes.
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* *(..)) && !target(DeepSamplerSpringConfig)")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        SampleDefinition sampleDefinition = findSampleDefinition(joinPoint);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            return sampleDefinition.getReturnValueSupplier().supply();
        } else {
            return joinPoint.proceed();
        }
    }

    /**
     * Searches for a {@link SampleDefinition} that matches to a particular method call.
     * @param proceedingJoinPoint
     * @return
     */
    private SampleDefinition findSampleDefinition(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Object interceptedObject = proceedingJoinPoint.getThis();

        SampledMethod sampledMethod = new SampledMethod(interceptedObject.getClass(), signature.getMethod());

        return SampleRepository.getInstance().find(sampledMethod, proceedingJoinPoint.getArgs());
    }


}
