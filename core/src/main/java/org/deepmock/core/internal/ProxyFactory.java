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

}
