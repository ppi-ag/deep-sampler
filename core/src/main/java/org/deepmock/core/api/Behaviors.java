package org.deepmock.core.api;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import org.deepmock.core.error.InvalidConfigException;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;

import java.lang.reflect.Array;
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

            List<ParameterMatcher> parameterMatchers = Arrays.stream(args)
                    .map(Behaviors::toMatcher)
                    .collect(Collectors.toList());

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
        if (cls.isPrimitive()) {
            return createEmptyPrimitive(cls);
        } else if (cls.isArray()) {
            return createEmptyArray(cls);
        }
        return createEmptyObjectProxy(cls);
    }

    private static Object createEmptyArray(Class<?> cls) {
        return Array.newInstance(cls, 0);
    }

    private static Object createEmptyObjectProxy(Class<?> cls) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(cls);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            return proxy.invokeSuper(obj, args);
        });
        return enhancer.create();
    }

    private static Object createEmptyPrimitive(Class<?> cls) {
        if (cls.isAssignableFrom(int.class)) {
            return Integer.valueOf(0);
        } else if (cls.isAssignableFrom(double.class)) {
            return Double.valueOf(0.0);
        } else if (cls.isAssignableFrom(float.class)) {
            return Float.valueOf(0.0f);
        } else if (cls.isAssignableFrom(short.class)) {
            return Short.valueOf((short) 0);
        } else if (cls.isAssignableFrom(byte.class)) {
            return Byte.valueOf((byte) 0);
        } else if (cls.isAssignableFrom(char.class)) {
            return Character.valueOf('0');
        }
        throw new InvalidConfigException("The unknown primitve '" + cls + "' appeared");
    }

    public static Quantity times(int i) {
        return null;
    }

}
