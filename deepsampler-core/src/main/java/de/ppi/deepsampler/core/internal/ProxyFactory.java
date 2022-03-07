/*
 * Copyright 2022 PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal;

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
        final var proxyFactory = new javassist.util.proxy.ProxyFactory();
        if (cls.isInterface()) {
            proxyFactory.setInterfaces(new Class[]{cls});
            proxyFactory.setSuperclass(Object.class);
        } else {
            proxyFactory.setSuperclass(cls);
        }
        final var proxyClass = proxyFactory.createClass();
        final var objenesis = new ObjenesisStd();
        final var instantiatorOf = objenesis.getInstantiatorOf(proxyClass);
        final var proxyObject = (ProxyObject) instantiatorOf.newInstance();
        proxyObject.setHandler(proxyBehavior);
        return (T) proxyObject;
    }



    public static boolean isProxyClass(final Class<?> aClass) {
        return javassist.util.proxy.ProxyFactory.isProxyClass(aClass);
    }
}
