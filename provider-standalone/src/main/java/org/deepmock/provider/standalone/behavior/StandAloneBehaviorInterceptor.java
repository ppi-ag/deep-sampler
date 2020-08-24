package org.deepmock.provider.standalone.behavior;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;
import org.deepmock.provider.standalone.aop.InterceptorBridge;
import org.deepmock.provider.standalone.aop.MethodInterceptor;
import org.deepmock.provider.standalone.aop.MethodInvocation;

import java.util.List;

public class StandAloneBehaviorInterceptor implements MethodInterceptor {


    @Override
    public Object intercept(MethodInvocation methodInvocation) {
        Behavior behavior = findBehavior(methodInvocation);

        if (behavior != null && argumentsMatch(behavior, methodInvocation.getParameter())) {
            return behavior.getReturnValueSupplier().supply();
        } else {
            return methodInvocation.proceed();
        }
    }

    private Behavior findBehavior(MethodInvocation invocation) {
        JoinPoint joinPoint = new JoinPoint(invocation.getTarget().getClass(), invocation.getOriginalMethod());
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
