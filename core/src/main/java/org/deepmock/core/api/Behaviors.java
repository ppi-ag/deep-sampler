package org.deepmock.core.api;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Behaviors {

    public static <T> T of(Class<T> cls) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);

        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            JoinPoint joinPoint = new JoinPoint(cls, method);
            Behavior behavior = new Behavior(joinPoint);

            List<ParameterMatcher> parameterMatchers = Arrays.stream(args).map(Behaviors::toMatcher).collect(Collectors.toList());
            behavior.setParameter(parameterMatchers);

            BehaviorRepository.getInstance().add(behavior);

            return createEmptyProxy(method.getReturnType());
        });

        return (T) enhancer.create();
    }

    private static ParameterMatcher toMatcher(Object parameterValue) {
        if (parameterValue instanceof ParameterMatcher) {
            return (ParameterMatcher) parameterValue;
        } else {
            return Matchers.specific(parameterValue);
        }
    }

    private static Object createEmptyProxy(Class<?> cls) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);
        return enhancer.create();
    }



    public static Quantity times(int i) {
        return null;
    }

}
