package org.deepsampler.core.internal;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

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

    public static boolean isProxyClass(Class<?> aClass) {
        return javassist.util.proxy.ProxyFactory.isProxyClass(aClass);
    }
}
