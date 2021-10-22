/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.core.internal.aophandler;

import de.ppi.deepsampler.core.api.Sampler;
import de.ppi.deepsampler.core.error.VerifyException;
import de.ppi.deepsampler.core.api.FixedQuantity;
import de.ppi.deepsampler.core.internal.api.ExecutionManager;
import de.ppi.deepsampler.core.model.MethodCall;
import de.ppi.deepsampler.core.model.SampleDefinition;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.core.model.SampledMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertThrows;

class VerifySampleHandlerTest {

    @BeforeEach
    public void cleanUp() {
        Sampler.clear();
    }

    @Test
    void testInvokeSamplePresentAndVerifyCorrect() throws NoSuchMethodException {
        // GIVEN
        final VerifySampleHandler verifySampleHandler = new VerifySampleHandler(new FixedQuantity(1), ProxyTest.class);
        final ProxyTest proxyTest = new ProxyTest();
        final Method simMethod = proxyTest.getClass().getMethod("test");
        final SampleDefinition behaviorTest = new SampleDefinition(new SampledMethod(proxyTest.getClass(), simMethod));
        SampleRepository.getInstance().add(behaviorTest);
        ExecutionManager.record(behaviorTest, new MethodCall(null, new ArrayList<>()));

        // WHEN
        // THEN
        Assertions.assertDoesNotThrow(() -> verifySampleHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    @Test
    void testInvokeSamplePresentAndVerifyNotCorrect() throws NoSuchMethodException {
        // GIVEN
        final VerifySampleHandler verifySampleHandler = new VerifySampleHandler(new FixedQuantity(2), ProxyTest.class);
        final ProxyTest proxyTest = new ProxyTest();
        final Method simMethod = proxyTest.getClass().getMethod("test");
        final SampleDefinition sampleDefinition = new SampleDefinition(new SampledMethod(proxyTest.getClass(), simMethod));
        SampleRepository.getInstance().add(sampleDefinition);
        ExecutionManager.record(sampleDefinition, new MethodCall(null, new ArrayList<>()));

        // WHEN
        // THEN
        assertThrows(VerifyException.class, () -> verifySampleHandler.invoke(proxyTest, simMethod, null, new Object[0]));
    }

    @Test
    void testInvokeSampleNotPresentPresent() throws NoSuchMethodException {
        // GIVEN
        final VerifySampleHandler verifySampleHandler = new VerifySampleHandler(new FixedQuantity(2), ProxyTest.class);
        final ProxyTest proxyTest = new ProxyTest();
        final Method simMethod = proxyTest.getClass().getMethod("test");

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