/*
 * Copyright 2021  PPI AG (Hamburg, Germany)
 * This program is made available under the terms of the MIT License.
 */

package de.ppi.deepsampler.junit5;

import de.ppi.deepsampler.core.api.Sample;
import de.ppi.deepsampler.core.internal.ProxyFactory;
import de.ppi.deepsampler.core.model.SampleRepository;
import de.ppi.deepsampler.junit.PrepareSampler;
import de.ppi.deepsampler.junit.TestBean;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DeepSamplerExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SuppressWarnings("unused")
class DeepSamplerExtensionTest {

    @PrepareSampler
    private TestBean testBeanSampler;

    @Test
    @Order(1)
    void testBeanHasBeenSampled() {
        assertNotNull(testBeanSampler);
        assertTrue(ProxyFactory.isProxyClass(testBeanSampler.getClass()));
    }

    @Test
    @Order(2)
    void addSamplesThatShouldBeClearedInNextTest() {
        assertTrue(SampleRepository.getInstance().isEmpty());
        Sample.of(testBeanSampler.getSomeInt()).is(1);
        assertFalse(SampleRepository.getInstance().isEmpty());
    }

    @Test
    @Order(3)
    void samplesFromLastTestShouldBeCleared() {
        assertTrue(SampleRepository.getInstance().isEmpty());
    }
}