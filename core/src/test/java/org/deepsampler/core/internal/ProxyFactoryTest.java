package org.deepsampler.core.internal;

import javassist.util.proxy.MethodHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyFactoryTest {

    @Test
    void testCreateProxy() {
        // GIVEN
        final AtomicInteger counter = new AtomicInteger();
        final MethodHandler methodHandler = new MethodHandler() {
            @Override
            public Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
                counter.incrementAndGet();
                return null;
            }
        };

        // WHEN
        final ProxyTest proxyTest = ProxyFactory.createProxy(ProxyTest.class, methodHandler);
        proxyTest.test();
        proxyTest.test();

        // THEN
        assertEquals(2, counter.get());
    }

    private static class ProxyTest {
        public void test() {
            // NOTHING TO DO
        }
    }
}