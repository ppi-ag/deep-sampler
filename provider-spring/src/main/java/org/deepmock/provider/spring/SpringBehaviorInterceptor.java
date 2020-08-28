package org.deepmock.provider.spring;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.deepmock.core.internal.api.ExecutionManager;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;
import org.deepmock.provider.common.BehaviorInterceptor;

import java.util.List;

/**
 * A BehaviorInterceptor for SpringApplications.
 */
@Aspect
public class SpringBehaviorInterceptor implements BehaviorInterceptor {


    /**
     * Intercepts all Methods in SpringBeans and delegates to behaviors that have been defined within test classes.
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* *(..)) && !target(DeepMockSpringConfig)")
    public Object aroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Behavior behavior = findBehavior(joinPoint);

        if (behavior != null) {
            ExecutionManager.notify(behavior);

            return behavior.getReturnValueSupplier().supply();
        } else {
            return joinPoint.proceed();
        }
    }

    /**
     * Searches for a Behavior that fits a certain method call.
     * @param proceedingJoinPoint
     * @return
     */
    private Behavior findBehavior(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        Object interceptedObject = proceedingJoinPoint.getThis();

        JoinPoint joinPoint = new JoinPoint(interceptedObject.getClass(), signature.getMethod());

        return BehaviorRepository.getInstance().find(joinPoint, proceedingJoinPoint.getArgs());
    }


}
