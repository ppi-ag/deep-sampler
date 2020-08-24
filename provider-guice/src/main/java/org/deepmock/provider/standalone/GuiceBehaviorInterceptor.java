package org.deepmock.provider.standalone;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.deepmock.core.internal.api.ExecutionManager;
import org.deepmock.core.model.*;

import java.util.Arrays;

public class GuiceBehaviorInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Behavior behavior = findBehavior(invocation);

        if (behavior != null) {
            ExecutionManager.notify(behavior);

            ReturnValueSupplier returnValueSupplier = behavior.getReturnValueSupplier();

            if (returnValueSupplier != null) {
                return behavior.getReturnValueSupplier().supply();
            } else {
                // no returnValueSupplier -> we have to log the invocations for recordings
                Object returnValue = invocation.proceed();
                ExecutionManager.log(behavior, new MethodCall(Arrays.asList(invocation.getArguments()),
                        returnValue));
                return returnValue;
            }
        }

        return invocation.proceed();
    }

    private Behavior findBehavior(MethodInvocation invocation) {
        JoinPoint joinPoint = new JoinPoint(invocation.getThis().getClass(), invocation.getMethod());
        return BehaviorRepository.getInstance().find(joinPoint, invocation.getArguments());
    }
}
