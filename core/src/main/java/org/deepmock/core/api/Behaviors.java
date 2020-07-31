package org.deepmock.core.api;

import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.deepmock.core.error.BaseException;
import org.deepmock.core.error.InvalidConfigException;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class Behaviors {

    public static void clear() {
        BehaviorRepository.getInstance().clear();
    }

    @SuppressWarnings("unchecked")
    public static <T> T of(Class<T> cls) {

        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(cls);
        Class<?> proxyClass = proxyFactory.createClass();
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<?> instantiatorOf = objenesis.getInstantiatorOf(proxyClass);
        ProxyObject proxyObject = (ProxyObject) instantiatorOf.newInstance();
        proxyObject.setHandler((self, method, proceed, args) -> {
            JoinPoint joinPoint = new JoinPoint(cls, method);
            Behavior behavior = new Behavior(joinPoint);

            List<ParameterMatcher> parameterMatchers = Arrays.stream(args)
                    .map(Behaviors::toMatcher)
                    .collect(Collectors.toList());

            behavior.setParameter(parameterMatchers);

            BehaviorRepository.getInstance().add(behavior);

            return createEmptyProxy(method.getReturnType());
        });

        return (T) proxyObject;
    }

    private static ParameterMatcher toMatcher(Object parameterValue) {
        if (parameterValue instanceof ParameterMatcher) {
            return (ParameterMatcher) parameterValue;
        } else {
            return Matchers.equalTo(parameterValue);
        }
    }

    private static Object createEmptyProxy(Class<?> cls) {
        if (cls.isPrimitive()) {
            return createEmptyPrimitive(cls);
        } else if (cls.isArray()) {
            return createEmptyArray(cls);
        }
        return null;
    }

    private static Object createEmptyArray(Class<?> cls) {
        return Array.newInstance(cls, 0);
    }

    @SuppressWarnings("UnnecessaryBoxing")
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
