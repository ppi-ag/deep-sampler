package de.ppi.deepsampler.core.internal;

import javassist.util.proxy.MethodHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProxyFactoryTest {

    @Test
    void testCreateProxy() {
        // GIVEN
        final AtomicInteger counter = new AtomicInteger();
        final MethodHandler methodHandler = (self, thisMethod, proceed, args) -> {
            counter.incrementAndGet();
            return null;
        };

        // WHEN
        final ProxyTest proxyTest = ProxyFactory.createProxy(ProxyTest.class, methodHandler);
        proxyTest.test();
        proxyTest.test();

        // THEN
        assertEquals(2, counter.get());
    }

    @Test
    void testCreateProxyInterface() {
        // GIVEN
        final AtomicInteger counter = new AtomicInteger();
        MethodHandler methodHandler = (self, thisMethod, proceed, args) -> {
            counter.incrementAndGet();
            return null;
        };

        // WHEN
        final InterfaceTest proxyTest = ProxyFactory.createProxy(InterfaceTest.class, methodHandler);
        proxyTest.test();
        proxyTest.test();

        // THEN
        assertEquals(2, counter.get());
    }

    public interface InterfaceTest {
        // MARKER
        void test();
    }
    private static class ProxyTest {
        public void test() {
            // NOTHING TO DO
        }
    }
}