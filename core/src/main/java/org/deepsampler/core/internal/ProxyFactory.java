package org.deepsampler.core.internal;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyObject;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

public class ProxyFactory {

    private ProxyFactory() {
        //This class is not intended to be instantiated, therefore the constructor is private.
    }

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<T> cls, final MethodHandler proxyBehavior) {
        final javassist.util.proxy.ProxyFactory proxyFactory = new javassist.util.proxy.ProxyFactory();
        proxyFactory.setSuperclass(cls);
        final Class<?> proxyClass = proxyFactory.createClass();
        final Objenesis objenesis = new ObjenesisStd();
        final ObjectInstantiator<?> instantiatorOf = objenesis.getInstantiatorOf(proxyClass);
        final ProxyObject proxyObject = (ProxyObject) instantiatorOf.newInstance();
        proxyObject.setHandler(proxyBehavior);

        return (T) proxyObject;
    }

    public static boolean isProxyClass(final Class<?> aClass) {
        return javassist.util.proxy.ProxyFactory.isProxyClass(aClass);
    }
}
