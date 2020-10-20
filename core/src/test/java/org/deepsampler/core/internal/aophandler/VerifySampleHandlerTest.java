package org.deepsampler.core.internal.aophandler;

import org.deepsampler.core.error.VerifyException;
import org.deepsampler.core.internal.FixedQuantity;
import org.deepsampler.core.internal.api.ExecutionManager;
import org.deepsampler.core.model.SampleDefinition;
import org.deepsampler.core.model.SampleRepository;
import org.deepsampler.core.model.SampledMethod;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class VerifySampleHandlerTest {

    @Test
    void testInvokeSamplePresentAndVerifyCorrect() throws NoSuchMethodException {
        // GIVEN
        VerifySampleHandler verifySampleHandler = new VerifySampleHandler(new FixedQuantity(1), ProxyTest.class);
        ProxyTest proxyTest = new ProxyTest();
        Method simMethod = proxyTest.getClass().getMethod("test");
        SampleDefinition behaviorTest = new SampleDefinition(new SampledMethod(proxyTest.getClass(), simMethod));
        SampleRepository.getInstance().add(behaviorTest);
        ExecutionManager.notify(behaviorTest);

        // WHEN
        // THEN
        assertDoesNotThrow(() -> verifySampleHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    @Test
    void testInvokeSamplePresentAndVerifyNotCorrect() throws NoSuchMethodException {
        // GIVEN
        VerifySampleHandler verifySampleHandler = new VerifySampleHandler(new FixedQuantity(2), ProxyTest.class);
        ProxyTest proxyTest = new ProxyTest();
        Method simMethod = proxyTest.getClass().getMethod("test");
        SampleDefinition sampleDefinition = new SampleDefinition(new SampledMethod(proxyTest.getClass(), simMethod));
        SampleRepository.getInstance().add(sampleDefinition);
        ExecutionManager.notify(sampleDefinition);

        // WHEN
        // THEN
        assertThrows(VerifyException.class, () -> verifySampleHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    @Test
    void testInvokeSampleNotPresentPresent() throws NoSuchMethodException {
        // GIVEN
        VerifySampleHandler verifySampleHandler = new VerifySampleHandler(new FixedQuantity(2), ProxyTest.class);
        ProxyTest proxyTest = new ProxyTest();
        Method simMethod = proxyTest.getClass().getMethod("test");

        // WHEN
        // THEN
        assertThrows(VerifyException.class, () -> verifySampleHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    private static class ProxyTest {
        public void test() {
            // NOTHING TO DO
        }
    }
}