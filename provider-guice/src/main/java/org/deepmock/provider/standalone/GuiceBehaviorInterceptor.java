package org.deepmock.provider.standalone;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;
import org.deepmock.provider.common.BehaviorInterceptor;
import org.deepmock.core.internal.api.ExecutionManager;
import org.deepmock.core.model.*;

import java.util.List;

public class GuiceBehaviorInterceptor implements BehaviorInterceptor, MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Behavior behavior = findBehavior(invocation);

        if (behavior != null) {
            ExecutionManager.notify(behavior);

            return behavior.getReturnValueSupplier().supply();
        } else {
            return invocation.proceed();
        }
    }

    private Behavior findBehavior(MethodInvocation invocation) {
        JoinPoint joinPoint = new JoinPoint(invocation.getThis().getClass(), invocation.getMethod());
        return BehaviorRepository.getInstance().find(joinPoint, invocation.getArguments());
    }
}
