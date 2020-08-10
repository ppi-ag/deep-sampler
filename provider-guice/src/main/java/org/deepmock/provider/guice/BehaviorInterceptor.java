package org.deepmock.provider.guice;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;

import java.util.List;

public class BehaviorInterceptor implements MethodInterceptor {


    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Behavior behavior = findBehavior(invocation);

        if (behavior != null && argumentsMatch(behavior, invocation.getArguments())) {
            return behavior.getReturnValueSupplier().supply();
        } else {
            return invocation.proceed();
        }
    }

    private Behavior findBehavior(MethodInvocation invocation) {
        JoinPoint joinPoint = new JoinPoint(invocation.getThis().getClass(), invocation.getMethod());
        return BehaviorRepository.getInstance().find(joinPoint);
    }

    private boolean argumentsMatch(Behavior behavior, Object[] arguments) {
        List<ParameterMatcher> parameterMatchers = behavior.getParameter();

        if (parameterMatchers.size() != arguments.length) {
            return false;
        }

        for (int i = 0; i < arguments.length; i++) {
            if (!parameterMatchers.get(0).matches(arguments[i])) {
                return false;
            }
        }

        return true;
    }
}
