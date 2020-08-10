package org.deepmock.core.internal;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.deepmock.core.api.Behaviors;
import org.deepmock.core.api.Matchers;
import org.deepmock.core.error.InvalidConfigException;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.deepmock.core.model.ParameterMatcher;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProxyFactory {
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> cls, MethodHandler proxyBehavior) {

        javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();
        proxyFactory.setSuperclass(cls);
        Class<?> proxyClass = proxyFactory.createClass();
        Objenesis objenesis = new ObjenesisStd();
        ObjectInstantiator<?> instantiatorOf = objenesis.getInstantiatorOf(proxyClass);
        ProxyObject proxyObject = (ProxyObject) instantiatorOf.newInstance();
        proxyObject.setHandler(proxyBehavior);

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
}
