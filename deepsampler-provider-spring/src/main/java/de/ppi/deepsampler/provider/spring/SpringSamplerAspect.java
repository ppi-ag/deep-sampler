/*
 * Copyright 2020  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.provider.spring;

import de.ppi.deepsampler.core.internal.api.ExecutionManager;
import de.ppi.deepsampler.core.model.*;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.Arrays;

/**
 * The {@link SpringSamplerAspect} is responsible for the actual stubbing. It intercepts methods and decides which methods should be stubbed
 * using the {@link SampleDefinition}s.
 * <br>
 * Subclasses must be annotated with {@link org.aspectj.lang.annotation.Aspect} in order to tell Spring that this class is an Aspect.
 * Additionally this class must be added to a SpringConfig and the SpringConfig itself must enable AspectJProxies using the annotation
 * {@link org.springframework.context.annotation.EnableAspectJAutoProxy}.
 * <br>
 * The intercepted (i.e. stubbed) classes are defined using an AspectJ PointCut. This is done
 * by implementing {@link SpringSamplerAspect#include()}. The method must be annotated with  {@link org.aspectj.lang.annotation.Pointcut}.
 * The annotation can then in turn define the classes that will be intercepted using the Pointcut expression language.
 * <br>
 * A short introduction in Pointcut expressions can be found here: https://www.baeldung.com/spring-aop-pointcut-tutorial.
 */
public abstract class SpringSamplerAspect {


    /**
     * This method is used to define the classes that will be intercepted. This is done using the annotation
     * {@link org.aspectj.lang.annotation.Pointcut}.
     * <br>
     * The organization of Pointcuts can be done by defining separate Pointcuts for layers, or modules of an application. Each Pointcut can
     * be defined using a method that is named according to a module or a layer. For instance:
     * <br>
     * <pre>
     * {@literal @}Pointcut("within(org.my.application.dao..*)")
     * public void daoLayer() {}
     *
     * {@literal @}Pointcut("within(org.my.application.simulation..*)")
     * public void simulationLayer() {}
     *
     * {@literal @}Pointcut("daoLayer() || simulationLayer()")
     * public void include() {}
     *
     * </pre>
     */
    public abstract void include();


    /**
     * Intercepts methods in SpringBeans and delegates to {@link SampleDefinition}s that have been defined within test classes.
     *
     * @param joinPoint The AOP-JoinPoint that describes which method will be called and which method can now be intercepted. I.e. this method
     *                  will become a stub if the method is described by a Sampler.
     * @return If the intercepted method is a stub, the return value is determined by the Sampler, otherwise this is the original return value coming from
     * the original method as described by the {@code joinPoint}.
     * @throws Throwable Since we intercept numerous methods in general, it is possible that any kind of {@link Exception}, even {@link Throwable} is thrown.
     * Even though declaring {@link Throwable} in a throws clause is usually not recommended, this is done by Spring itself (for comprehensible reasons),
     * so we are also forced to do so.
     */
    @Around("!@within(org.springframework.context.annotation.Configuration) " // excludes all SpringConfigs
            + "&& !@within(org.aspectj.lang.annotation.Aspect) " // Excludes all Aspects by excluding classes annotated with @Aspect
            + "&& !within(is(EnumType)) " // Exludes all Enums
            + "&& !within(is(FinalType)) " // Exludes all final classes
            + "&& include()") // Delegates to the custom Pointcut that must be defined by overriding the Method SpringSamplerAspect::include
    public Object aroundMethod(final ProceedingJoinPoint joinPoint) throws Throwable {
        final SampleDefinition sampleDefinition = findSampleDefinition(joinPoint);

        if (sampleDefinition != null) {
            ExecutionManager.notify(sampleDefinition);

            final Answer<?> answer = sampleDefinition.getAnswer();

            if (answer != null) {
                final StubMethodInvocation stubMethodInvocation = new StubMethodInvocation(Arrays.asList(joinPoint.getArgs()),
                        joinPoint.getThis(),
                        joinPoint::proceed);
                return ExecutionManager.execute(sampleDefinition, stubMethodInvocation);
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

        // It is important not to take the target type from proceedingJoinPoint.getTarget() because this could return
        // another Proxy if other Aspects are running on the same method. In those cases the type would be the type of some
        // generic Proxy-class. Using the signature doesn't have this problem.
        final SampledMethod sampledMethod = new SampledMethod(signature.getDeclaringType(), signature.getMethod());

        return SampleRepository.getInstance().findValidated(sampledMethod, proceedingJoinPoint.getArgs());
    }


}
