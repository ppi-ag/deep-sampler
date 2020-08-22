package org.deepmock.core.internal.handler;

import org.deepmock.core.error.VerifyException;
import org.deepmock.core.internal.FixedQuantity;
import org.deepmock.core.internal.api.ExecutionManager;
import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.JoinPoint;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class VerifyBehaviorHandlerTest {

    @Test
    void testInvokeBehaviorPresentAndVerifyCorrect() throws NoSuchMethodException {
        // GIVEN
        VerifyBehaviorHandler verifyBehaviorHandler = new VerifyBehaviorHandler(new FixedQuantity(1), ProxyTest.class);
        ProxyTest proxyTest = new ProxyTest();
        Method simMethod = proxyTest.getClass().getMethod("test");
        Behavior behaviorTest = new Behavior(new JoinPoint(proxyTest.getClass(), simMethod));
        BehaviorRepository.getInstance().add(behaviorTest);
        ExecutionManager.notify(behaviorTest);

        // WHEN
        // THEN
        assertDoesNotThrow(() -> verifyBehaviorHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    @Test
    void testInvokeBehaviorPresentAndVerifyNotCorrect() throws NoSuchMethodException {
        // GIVEN
        VerifyBehaviorHandler verifyBehaviorHandler = new VerifyBehaviorHandler(new FixedQuantity(2), ProxyTest.class);
        ProxyTest proxyTest = new ProxyTest();
        Method simMethod = proxyTest.getClass().getMethod("test");
        Behavior behaviorTest = new Behavior(new JoinPoint(proxyTest.getClass(), simMethod));
        BehaviorRepository.getInstance().add(behaviorTest);
        ExecutionManager.notify(behaviorTest);

        // WHEN
        // THEN
        assertThrows(VerifyException.class, () -> verifyBehaviorHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    @Test
    void testInvokeBehaviorNotPresentPresent() throws NoSuchMethodException {
        // GIVEN
        VerifyBehaviorHandler verifyBehaviorHandler = new VerifyBehaviorHandler(new FixedQuantity(2), ProxyTest.class);
        ProxyTest proxyTest = new ProxyTest();
        Method simMethod = proxyTest.getClass().getMethod("test");

        // WHEN
        // THEN
        assertThrows(VerifyException.class, () -> verifyBehaviorHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    private static class ProxyTest {
        public void test() {
            // NOTHING TO DO
        }
    }
}